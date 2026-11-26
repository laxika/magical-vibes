package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;

@CardRegistration(set = "DRK", collectorNumber = "4")
public class Cleansing extends Card {

    public Cleansing() {
        // For each land, destroy that land unless any player pays 1 life.
        addSpellEffect(UnlessPaysEffect.forEach(Subjects.allLands(), Subjects.eachPlayer(), new PayLifeCost(1),
                DestroyTargetPermanentEffect::new));
    }
}
