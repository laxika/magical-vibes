package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "REX", collectorNumber = "16")
@CardRegistration(set = "REX", collectorNumber = "41")
public class OwenGradyRaptorTrainer extends Card {

    private static final String PARTNER_CARD_NAME = "Blue, Loyal Raptor";

    public OwenGradyRaptorTrainer() {
        // Partner with Blue, Loyal Raptor. When this creature enters, target player may search their
        // library for Blue, Loyal Raptor, reveal it, put it into their hand, then shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(PARTNER_CARD_NAME),
                "Have target player put Blue, Loyal Raptor into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ChooseOneForTargetCreatureEffect(List.of(
                        counterMode("Put a reach counter on it", CounterType.REACH),
                        counterMode("Put a menace counter on it", CounterType.MENACE),
                        counterMode("Put a trample counter on it", CounterType.TRAMPLE),
                        counterMode("Put a haste counter on it", CounterType.HASTE)))),
                "{T}: Put your choice of a reach, menace, trample, or haste counter on target Dinosaur. "
                        + "Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR),
                        "Target must be a Dinosaur"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    private static ChooseOneEffect.ChooseOneOption counterMode(String label, CounterType counterType) {
        return new ChooseOneEffect.ChooseOneOption(label,
                new PutCounterOnTargetPermanentEffect(counterType));
    }
}
