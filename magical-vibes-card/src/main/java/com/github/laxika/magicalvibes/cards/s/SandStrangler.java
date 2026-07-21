package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "107")
public class SandStrangler extends Card {

    public SandStrangler() {
        // When this creature enters, if you control a Desert or there is a Desert card in your
        // graveyard, you may have this creature deal 3 damage to target creature. Intervening-if
        // (CR 603.4) via ConditionalEffect(AnyOf(...)); the may only decides whether to deal the
        // damage after the creature target is chosen as the trigger goes on the stack.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new AnyOf(List.of(
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DESERT)),
                        new GraveyardCardThreshold(1, new CardSubtypePredicate(CardSubtype.DESERT))
                )),
                new MayEffect(new DealDamageToTargetCreatureEffect(3),
                        "Deal 3 damage to target creature?")));
    }
}
