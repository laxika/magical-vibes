package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "EVE", collectorNumber = "108")
public class NoggleHedgeMage extends Card {

    public NoggleHedgeMage() {
        // When this creature enters, if you control two or more Islands, you may tap two target
        // permanents. Intervening-if gate (CR 603.4): checked as the trigger goes on the stack and
        // again at resolution. Per the ruling, exactly two legal targets are chosen when the ability
        // goes on the stack (group 0, min=max=2); the "you may" only decides whether to tap them.
        target(new PermanentPredicateTargetFilter(new PermanentTruePredicate(),
                "Target must be a permanent"), 2, 2)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                        new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                        new MayEffect(new TapPermanentsEffect(TapUntapScope.TARGET),
                                "Tap two target permanents?")));

        // When this creature enters, if you control two or more Mountains, you may have this creature
        // deal 2 damage to target player or planeswalker. Independent intervening-if gate; targets a
        // player or planeswalker (group 1). The two gates fire independently — the ETB multi-target
        // pipeline skips whichever group's effect was gated out.
        target(new PermanentPredicateTargetFilter(new PermanentIsPlaneswalkerPredicate(),
                "Target must be a player or planeswalker"), 1, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                        new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)),
                        new MayEffect(new DealDamageToTargetPlayerOrPlaneswalkerEffect(2),
                                "Deal 2 damage to target player or planeswalker?")));
    }
}
