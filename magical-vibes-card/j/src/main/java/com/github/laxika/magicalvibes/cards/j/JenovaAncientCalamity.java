package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "228")
@CardRegistration(set = "FIN", collectorNumber = "346")
@CardRegistration(set = "FIN", collectorNumber = "493")
public class JenovaAncientCalamity extends Card {

    public JenovaAncientCalamity() {
        PermanentPredicateTargetFilter otherCreature = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Target must be another creature"
        );
        target(otherCreature, 0, 1)
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE,
                                new SourcePower()),
                        new GrantSubtypeToTargetCreatureEffect(CardSubtype.MUTANT));

        // "Whenever a Mutant you control dies during your turn" — Jenova herself counts while she is a Mutant.
        CardEffect drawForDyingMutant = new DrawCardEffect(new EventValue());
        addTrigger(Triggers.creatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.MUTANT),
                        ObjectRelation.YOU_CONTROL).onlyDuringYourTurn(),
                drawForDyingMutant);
    }
}
