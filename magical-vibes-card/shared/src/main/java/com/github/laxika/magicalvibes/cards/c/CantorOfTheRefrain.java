package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostOwnedCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastWithWarpCostPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "9")
public class CantorOfTheRefrain extends Card {

    public CantorOfTheRefrain() {
        addStaticEffect(new CantBlockEffect());

        // When you cast a spell for its warp cost, return this card from your graveyard to the
        // battlefield under its owner's control. It perpetually gets +1/+0.
        addTrigger(new TriggerCondition(Triggers.youCastSpell(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, new SpellCastTriggerEffect(
                null,
                List.of(SequenceEffect.of(
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(false),
                        new PerpetuallyBoostOwnedCardsEffect(new CardIsSelfPredicate(), 1, 0))),
                new StackEntryCastWithWarpCostPredicate()));
    }
}
