package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostOwnedCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastWithWarpCostPredicate;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "9")
public class CantorOfTheRefrain extends Card {

    public CantorOfTheRefrain() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());

        // When you cast a spell for its warp cost, return this card from your graveyard to the
        // battlefield under its owner's control. It perpetually gets +1/+0.
        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(SequenceEffect.of(
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(false),
                        new PerpetuallyBoostOwnedCardsEffect(new CardIsSelfPredicate(), 1, 0))),
                new StackEntryCastWithWarpCostPredicate()));
    }
}
