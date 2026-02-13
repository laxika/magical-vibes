package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

public record PreventNextColorDamageToControllerEffect(CardColor chosenColor) implements CardEffect {

    public PreventNextColorDamageToControllerEffect() {
        this(null);
    }
}
