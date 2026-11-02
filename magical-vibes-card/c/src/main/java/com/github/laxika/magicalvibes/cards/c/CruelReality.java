package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrLoseLifeEffect;
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
        // At the beginning of enchanted player's upkeep, that player sacrifices a creature or
        // planeswalker of their choice. If the player can't, they lose 5 life.
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER), new SacrificePermanentOrLoseLifeEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                5));
    }
}
