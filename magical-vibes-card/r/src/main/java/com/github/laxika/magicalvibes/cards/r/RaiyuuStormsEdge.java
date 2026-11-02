package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.FirstCombatPhase;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "232")
public class RaiyuuStormsEdge extends Card {

    public RaiyuuStormsEdge() {
        CardAnyOfPredicate samuraiOrWarrior = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.SAMURAI),
                new CardSubtypePredicate(CardSubtype.WARRIOR)));

        addTrigger(Triggers.creatureYouControlAttacks(),
                new TriggeringCardConditionalEffect(samuraiOrWarrior,
                        new ConditionalEffect(new AttacksAlone(),
                                new UntapPermanentsEffect(TapUntapScope.TRIGGERING))));
        addTrigger(Triggers.creatureYouControlAttacks(),
                new TriggeringCardConditionalEffect(samuraiOrWarrior,
                        new ConditionalEffect(new AttacksAlone(),
                                new ConditionalEffect(new FirstCombatPhase(),
                                        new AdditionalCombatPhaseEffect(1)))));
    }
}
