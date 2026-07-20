package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrLoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "84")
public class CruelReality extends Card {

    public CruelReality() {
        // At the beginning of enchanted player's upkeep, that player sacrifices a creature or
        // planeswalker of their choice. If the player can't, they lose 5 life.
        addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED, new SacrificePermanentOrLoseLifeEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                5));
    }
}
