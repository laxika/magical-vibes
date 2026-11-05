package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.FirstCombatPhase;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "232")
public class RaiyuuStormsEdge extends Card {

    public RaiyuuStormsEdge() {
        PermanentPredicate samuraiOrWarrior = new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.SAMURAI),
                new PermanentHasSubtypePredicate(CardSubtype.WARRIOR)));

        addTrigger(Triggers.creatureYouControlAttacks().withFilter(samuraiOrWarrior),
                TriggerOptions.DEFAULT.withInterveningIf(new AttacksAlone()),
                new UntapPermanentsEffect(TapUntapScope.TRIGGERING),
                new ConditionalEffect(new FirstCombatPhase(), new AdditionalCombatPhaseEffect(1)));
    }
}
