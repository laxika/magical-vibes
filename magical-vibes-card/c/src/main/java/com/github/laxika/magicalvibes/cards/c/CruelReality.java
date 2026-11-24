package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "84")
@CardRegistration(set = "AKR", collectorNumber = "98")
@CardRegistration(set = "SLZ", collectorNumber = "36")
@CardRegistration(set = "SLZ", collectorNumber = "157")
@CardRegistration(set = "SLZ", collectorNumber = "278")
public class CruelReality extends Card {

    public CruelReality() {
        // "That player" is the enchanted player whose upkeep it is.
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER), SequenceEffect.of(
                new SacrificePermanentsEffect(1, new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())), Subjects.activePlayer()),
                new ConditionalEffect(new NotCondition(HappenedThisWay.sacrificed()),
                        new LoseLifeEffect(5, Subjects.activePlayer()))));
    }
}
