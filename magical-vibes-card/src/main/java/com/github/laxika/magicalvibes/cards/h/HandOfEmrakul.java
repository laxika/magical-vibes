package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "5")
public class HandOfEmrakul extends Card {

    public HandOfEmrakul() {
        addCastingOption(new AlternateHandCast(List.of(
                new SacrificePermanentsCost(4, new PermanentHasSubtypePredicate(CardSubtype.SPAWN)))));

        addEffect(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                1, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER));
    }
}
