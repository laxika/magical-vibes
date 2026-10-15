package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "26")
@CardRegistration(set = "MKC", collectorNumber = "336")
public class CharnelSerenade extends Card {

    public CharnelSerenade() {
        addEffect(EffectSlot.SPELL, SurveilThenEffect.direct(
                3,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .enterWithCounter(CounterType.FINALITY)
                        .enterWithCounterCount(1)
                        .build()));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect(3));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(),
                "Suspend 3—{2}{B}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(3));
    }
}
