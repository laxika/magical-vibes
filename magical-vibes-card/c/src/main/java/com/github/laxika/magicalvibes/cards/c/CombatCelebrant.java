package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceExertedThisTurn;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "125")
@CardRegistration(set = "AKR", collectorNumber = "148")
@CardRegistration(set = "LTC", collectorNumber = "212")
@CardRegistration(set = "BLC", collectorNumber = "194")
public class CombatCelebrant extends Card {

    public CombatCelebrant() {
        // "If this creature hasn't been exerted this turn, you may exert it as it attacks. When you
        // do, untap all other creatures you control and after this phase, there is an additional
        // combat phase." The extra combat has no extra main phase.
        addTrigger(Triggers.attacks(), TriggerOptions.DEFAULT.withEventCondition(
                new NotCondition(new SourceExertedThisTurn())),
                new MayEffect(
                        SequenceEffect.of(
                                SkipNextUntapEffect.exert(),
                                new QueueReflexiveAbilityEffect(SequenceEffect.of(
                                        new UntapPermanentsEffect(TapUntapScope.OTHER_CONTROLLED_CREATURES),
                                        new AdditionalCombatPhaseEffect(1)))
                        ),
                        "Exert Combat Celebrant as it attacks? (Untap all other creatures you control and take an additional combat phase.)"
                ));
    }
}
