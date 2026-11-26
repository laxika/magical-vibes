package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "4ED", collectorNumber = "70")
@CardRegistration(set = "DRK", collectorNumber = "26")
public class Erosion extends Card {

    public Erosion() {
        // Enchant land. At the beginning of the upkeep of enchanted land's controller,
        // destroy that land unless that player pays {1} or 1 life.
        target(TargetFilters.land()).addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                UnlessPaysEffect.unlessPays(Subjects.activePlayer(),
                        AnyOfCost.of(new PayManaCost("{1}"), new PayLifeCost(1)),
                        new DestroyTargetPermanentEffect(Subjects.attached())));
    }
}
