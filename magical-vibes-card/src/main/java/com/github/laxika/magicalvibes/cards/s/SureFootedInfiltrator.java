package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "83")
public class SureFootedInfiltrator extends Card {

    public SureFootedInfiltrator() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(1,
                                new PermanentHasSubtypePredicate(CardSubtype.ROGUE), true),
                        new MakeCreatureUnblockableEffect(true)
                ),
                "Tap another untapped Rogue you control: This creature can't be blocked this turn."
        ));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect(1));
    }
}
