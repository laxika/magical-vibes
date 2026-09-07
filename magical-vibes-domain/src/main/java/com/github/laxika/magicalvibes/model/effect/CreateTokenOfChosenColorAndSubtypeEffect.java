package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Map;

public record CreateTokenOfChosenColorAndSubtypeEffect(
        DynamicAmount power,
        DynamicAmount toughness,
        Map<CardColor, CardSubtype> subtypeByColor
) implements TokenCreatingEffect {

    public CreateTokenOfChosenColorAndSubtypeEffect() {
        this(new Fixed(2), new Fixed(2), Map.of());
    }

    public CreateTokenOfChosenColorAndSubtypeEffect(int power, int toughness,
                                                    Map<CardColor, CardSubtype> subtypeByColor) {
        this(new Fixed(power), new Fixed(toughness), subtypeByColor);
    }

    public CreateTokenOfChosenColorAndSubtypeEffect(DynamicAmount power, DynamicAmount toughness) {
        this(power, toughness, Map.of());
    }

    public CreateTokenOfChosenColorAndSubtypeEffect {
        subtypeByColor = subtypeByColor == null ? Map.of() : Map.copyOf(subtypeByColor);
    }

    @Override
    public DynamicAmount tokenAmount() {
        return new Fixed(1);
    }

    @Override
    public CardType tokenType() {
        return CardType.CREATURE;
    }

    @Override
    public int tokenPower() {
        return power instanceof Fixed fixed ? fixed.value() : 0;
    }

    @Override
    public int tokenToughness() {
        return toughness instanceof Fixed fixed ? fixed.value() : 0;
    }
}
