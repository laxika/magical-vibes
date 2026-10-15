package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongOwnedCommanders;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIC", collectorNumber = "37")
@CardRegistration(set = "MIC", collectorNumber = "75")
public class VisionsOfDominance extends Card {

    public VisionsOfDominance() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))
                .addEffect(EffectSlot.SPELL, new DoublePlusOneCountersOnTargetCreatureEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new CastFromZone(Zone.GRAVEYARD),
                new ReduceOwnCastCostEffect(new GreatestManaValueAmongOwnedCommanders())));
        addCastingOption(new FlashbackCast("{8}{G}{G}"));
    }
}
