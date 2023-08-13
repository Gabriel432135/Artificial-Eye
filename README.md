# Artificial Eye

Este é o meu primeiro projeto que utiliza visão computacional e processamento de imagem com OpenCV.

## Descrição

O projeto "Artificial Eye" é uma aplicação Android que utiliza a câmera do dispositivo para detectar rostos em tempo real. A técnica usada é o "haarcascade"

## Funcionalidades

- Detecção em tempo real de rostos utilizando a câmera do dispositivo.
- Utiliza a biblioteca OpenCV para a detecção de objetos.
- Desenha retângulos verdes ao redor dos rostos detectados.

## Classe Principal

A classe principal do projeto é `MainActivity.java`, onde ocorre a inicialização da câmera, processamento de imagens e detecção de rostos.

## Configuração

Para utilizar o projeto, é necessário ter o Android Studio instalado. Além disso, a biblioteca OpenCV 4.8.0 precisa ser manualmente importada no projeto.

## Como Usar

1. Clone o repositório para o seu ambiente de desenvolvimento.
2. Abra o projeto no Android Studio.
3. Realize a importação manual da biblioteca OpenCV 4.8.0 no projeto.
4. Execute o aplicativo em um dispositivo ou emulador Android.

## Observações

1. Certifique-se de ajustar o diretório da biblioteca OpenCV de acordo com a sua configuração. Verifique o método `loadXML()` na classe `MainActivity.java`.
2. Infelizmente alguns dispositivos são incompatíveis com a implementação atual do CameraX e disparam exception ao tentar vincular o Analyzer ao ciclo de vida da câmera, como o LG K50S. Talvez uma atualização do CameraX resolva isso no futuro.

## Autor

Gabriel Alves
