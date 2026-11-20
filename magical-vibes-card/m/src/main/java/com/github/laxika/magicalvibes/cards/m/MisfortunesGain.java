package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PTK", collectorNumber = "13")
@CardRegistration(set = "ME3", collectorNumber = "21")
public class MisfortunesGain extends Card {

    public MisfortunesGain() {
        // Destroy target creature. Its owner gains 4 life. Life routes to the creature's owner
        // (the original controller absent any control-changing effect), not the current controller.
        target(TargetFilters.creature()).addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new GainLifeEffect(4, Subjects.ownerOf(Subjects.target())));
    }
}
