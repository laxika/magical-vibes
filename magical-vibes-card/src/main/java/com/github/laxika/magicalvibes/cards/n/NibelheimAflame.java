package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "FIN", collectorNumber = "146")
public class NibelheimAflame extends Card {

    public NibelheimAflame() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature you control"
        )).addEffect(EffectSlot.SPELL, new TargetCreatureDealsPowerDamageToEachOtherCreatureEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastFromZone(Zone.GRAVEYARD),
                new DiscardOwnHandThenDrawEffect(new Fixed(4))));
        addCastingOption(new FlashbackCast("{5}{R}{R}"));
    }
}
