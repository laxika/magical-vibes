package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "32")
public class MasterApothecary extends Card {

    public MasterApothecary() {
        // Tap an untapped Cleric you control: Prevent the next 2 damage that would be dealt to any target this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.CLERIC)),
                        PreventDamageEffect.nextToTarget(2)
                ),
                "Tap an untapped Cleric you control: Prevent the next 2 damage that would be dealt to any target this turn."
        ));
    }
}
