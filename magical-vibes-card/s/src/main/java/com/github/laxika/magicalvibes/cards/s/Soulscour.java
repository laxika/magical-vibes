package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "DST", collectorNumber = "14")
public class Soulscour extends Card {

    public Soulscour() {
        addSpellEffect(DestroyTargetPermanentEffect.all(
                new PermanentNotPredicate(new PermanentIsArtifactPredicate())));
    }
}
