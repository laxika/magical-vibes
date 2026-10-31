package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MBS", collectorNumber = "78")
public class CreepingCorrosion extends Card {

    public CreepingCorrosion() {
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentIsArtifactPredicate()));
    }
}
