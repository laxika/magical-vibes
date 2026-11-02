package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "77")
@CardRegistration(set = "FIN", collectorNumber = "361")
public class SummonLeviathan extends Card {

    public SummonLeviathan() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));

        var seaCreature = new PermanentHasAnySubtypePredicate(Set.of(
                CardSubtype.KRAKEN, CardSubtype.LEVIATHAN, CardSubtype.MERFOLK,
                CardSubtype.OCTOPUS, CardSubtype.SERPENT));

        addTrigger(Triggers.chapter(1), ReturnToHandEffect.allPermanentsMatching(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(seaCreature)
                ))));

        var drawOnSeaCreatureAttack = new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                new TriggeredAbility(Triggers.creatureAttacks(),
                new TriggeringPermanentConditionalEffect(seaCreature, new DrawCardEffect(1))));
        addTrigger(Triggers.chapter(2), drawOnSeaCreatureAttack);
        addTrigger(Triggers.chapter(3), drawOnSeaCreatureAttack);
    }
}
