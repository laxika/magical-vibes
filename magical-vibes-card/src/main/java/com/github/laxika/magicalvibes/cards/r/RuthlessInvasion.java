package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "NPH", collectorNumber = "93")
public class RuthlessInvasion extends Card {

    public RuthlessInvasion() {
        addEffect(EffectSlot.SPELL, new CantBlockThisTurnEffect(new PermanentNotPredicate(new PermanentIsArtifactPredicate())));
    }
}
