package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OwnsCardInAllZones;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealOwnHandEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "57")
public class HedronAlignment extends Card {

    public HedronAlignment() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                SequenceEffect.of(
                        new RevealOwnHandEffect(),
                        ConditionalEffect.unless(
                                new OwnsCardInAllZones(new CardNamedPredicate("Hedron Alignment")),
                                new WinGameEffect())),
                "Reveal your hand?"));

        addActivatedAbility(new ActivatedAbility(false, "{1}{U}", List.of(new ScryEffect(1)),
                "{1}{U}: Scry 1."));
    }
}
