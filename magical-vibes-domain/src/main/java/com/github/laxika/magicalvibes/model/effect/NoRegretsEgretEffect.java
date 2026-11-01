package com.github.laxika.magicalvibes.model.effect;

/** No-Regrets Egret's optional pregame look at the top two cards. */
public record NoRegretsEgretEffect() implements MulliganActionEffect {

    @Override
    public String mulliganActionDescription() {
        return "Reveal it and look at the top two cards of your library?";
    }
}
