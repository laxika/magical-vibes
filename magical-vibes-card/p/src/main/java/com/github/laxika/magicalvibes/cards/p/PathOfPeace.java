package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "POR", collectorNumber = "21")
@CardRegistration(set = "P02", collectorNumber = "18")
@CardRegistration(set = "USG", collectorNumber = "29")
@CardRegistration(set = "S99", collectorNumber = "21")
@CardRegistration(set = "A25", collectorNumber = "29")
public class PathOfPeace extends Card {

    public PathOfPeace() {
        // Destroy target creature. Its owner gains 4 life. Life routes to the creature's owner
        // (the original controller absent any control-changing effect), not the current controller.
        target(TargetFilters.creature()).addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new GainLifeEffect(4, Subjects.ownerOf(Subjects.target())));
    }
}
