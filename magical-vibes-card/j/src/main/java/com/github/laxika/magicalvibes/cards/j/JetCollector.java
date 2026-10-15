package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OnceOnlyTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YOTJ", collectorNumber = "18")
public class JetCollector extends Card {

    public JetCollector() {
        addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED, new ConditionalEffect(
                new GraveyardCardThreshold(4, null),
                new OnceOnlyTriggerEffect(new ConjureCardNamedIntoHandEffect("Mox Jet", false))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .requiresManaValueEqualsX(true)
                        .enterWithCounter(CounterType.FINALITY)
                        .enterWithCounterCount(1)
                        .build()),
                "{X}{B}: Return target creature card with mana value X from your graveyard to the battlefield with a finality counter on it. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withXValue());
    }
}
