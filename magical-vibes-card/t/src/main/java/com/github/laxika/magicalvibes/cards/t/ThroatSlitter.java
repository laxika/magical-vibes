package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BOK", collectorNumber = "88")
@CardRegistration(set = "PC2", collectorNumber = "37")
@CardRegistration(set = "PCA", collectorNumber = "37")
@CardRegistration(set = "SLD", collectorNumber = "2356")
@CardRegistration(set = "SLD", collectorNumber = "2360")
public class ThroatSlitter extends Card {

    public ThroatSlitter() {
        // Ninjutsu {2}{B}
        addNinjutsu("{2}{B}");

        // Whenever this creature deals combat damage to a player, destroy target nonblack creature
        // that player controls.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DestroyTargetPermanentEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))
                        )),
                        new PermanentControlledByTriggeringPlayerPredicate()))));
    }
}
