package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.NonArtifactNonColorCreatureTargetFilter;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "182")
public class Terror extends Card {

    public Terror() {
        setNeedsTarget(true);
        setTargetFilter(new NonArtifactNonColorCreatureTargetFilter(Set.of(CardColor.BLACK)));
        addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(Set.of(CardType.CREATURE), true));
    }
}
