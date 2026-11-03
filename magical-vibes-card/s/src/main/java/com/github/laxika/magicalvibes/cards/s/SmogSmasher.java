package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetCreatureIntoExileEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YDFT", collectorNumber = "16")
public class SmogSmasher extends Card {

    public SmogSmasher() {
        PermanentAllOfPredicate nontokenCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())));

        target(new PermanentPredicateTargetFilter(nontokenCreature,
                "Target must be a nontoken creature")).addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                        TriggerOptions.ONE_OR_MORE,
                        new ConjureDuplicateOfTargetCreatureIntoExileEffect());

        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new MaxSpeed()),
                new ReturnAllCardsExiledWithSourceEffect(true, null, false, Set.of(Keyword.HASTE),
                        null, null, null, null, false, false, true));
    }
}
