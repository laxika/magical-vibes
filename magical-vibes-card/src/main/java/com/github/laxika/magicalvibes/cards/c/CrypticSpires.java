package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "2X2", collectorNumber = "332")
public class CrypticSpires extends Card {

    public CrypticSpires() {
        this(List.of());
    }

    /** Builds a physical card with the colors circled before the game. */
    public CrypticSpires(List<ManaColor> circledColors) {
        circledColors = List.copyOf(circledColors);
        if (!circledColors.isEmpty() && (circledColors.size() != 2
                || circledColors.getFirst() == circledColors.getLast()
                || !ManaColor.COLORS.containsAll(circledColors))) {
            throw new IllegalArgumentException("Circle two different colors before the game.");
        }
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(circledColors)),
                "{T}: Add one mana of either of the circled colors."
        ));
    }
}
