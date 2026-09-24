package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantCastAdditionalNonartifactSpellsEffect;

@CardRegistration(set = "ALA", collectorNumber = "10")
@CardRegistration(set = "MMA", collectorNumber = "14")
@CardRegistration(set = "SLD", collectorNumber = "2102")
public class EtherswornCanonist extends Card {

    public EtherswornCanonist() {
        // Each player who has cast a nonartifact spell this turn can't cast additional nonartifact spells.
        addEffect(EffectSlot.STATIC, new CantCastAdditionalNonartifactSpellsEffect());
    }
}
