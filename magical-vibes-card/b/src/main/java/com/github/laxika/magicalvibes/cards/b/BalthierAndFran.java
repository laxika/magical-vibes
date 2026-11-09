package com.github.laxika.magicalvibes.cards.b;

import java.util.List;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.filter.PermanentCrewedThisTurnBySourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.condition.FirstCombatPhase;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "213")
@CardRegistration(set = "FIN", collectorNumber = "319")
@CardRegistration(set = "FIN", collectorNumber = "393")
@CardRegistration(set = "FIN", collectorNumber = "477")
@CardRegistration(set = "FIN", collectorNumber = "538")
public class BalthierAndFran extends Card {

    public BalthierAndFran() {
        addStaticEffect(new StaticBoostEffect(
                1, 1, Set.of(Keyword.REACH, Keyword.VIGILANCE), GrantScope.OWN_PERMANENTS,
                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)));
        addTrigger(Triggers.creatureAttacks().withFilter(new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.VEHICLE),
                        new PermanentCrewedThisTurnBySourcePredicate()))),
                TriggerOptions.DEFAULT.withInterveningIf(new FirstCombatPhase()),
                new MayPayManaEffect("{1}{R}{G}", new AdditionalCombatPhaseEffect(1),
                        "Pay {1}{R}{G} to get an additional combat phase?"));
    }
}
