package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;

@CardRegistration(set = "ZNR", collectorNumber = "228")
public class MossPitSkeleton extends Card {

    public MossPitSkeleton() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{3}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new Kicked(),
                        new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3))));
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_CREATURE,
                new MayEffect(
                        new ConditionalEffect(new SourceCardInGraveyard(),
                                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0)),
                        "Put Moss-Pit Skeleton on top of your library?"));
    }
}
