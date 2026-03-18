package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintedCardNameMatchesEnteringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "NPH", collectorNumber = "87")
public class InvaderParasite extends Card {

    public InvaderParasite() {
        // Imprint — When Invader Parasite enters the battlefield, exile target land.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Target must be a land"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTargetPermanentAndImprintEffect());

        // Whenever a land with the same name as the exiled card enters the battlefield
        // under an opponent's control, Invader Parasite deals 2 damage to that player.
        addEffect(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD,
                new ImprintedCardNameMatchesEnteringPermanentConditionalEffect(
                        new DealDamageToTargetPlayerEffect(2)
                ));
    }
}
