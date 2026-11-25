package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantPlayerStaticEffectsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessGreaterThanPowerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "220")
public class TheKingpinOfCrime extends Card {

    public TheKingpinOfCrime() {
        addTrigger(Triggers.youCastSpell().anySpell(),
                UnlessPaysEffect.youMayPay("{W/B}", new LoseLifeEffect(1, Subjects.eachOpponent(), true))
                        .withPrompt("Pay {W/B} to extort?"));

        addTrigger(Triggers.youAttack(), UnlessPaysEffect.youMayPayLife(2, new GrantPlayerStaticEffectsUntilEndOfTurnEffect(List.of(
                        new AssignCombatDamageWithToughnessEffect(
                                Subjects.creaturesYouControl(),
                                new PermanentToughnessGreaterThanPowerPredicate())))).withPrompt("Pay 2 life to have creatures you control assign combat damage using toughness?"));
    }
}
