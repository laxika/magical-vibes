package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.NonArtifactNonColorCreatureTargetFilter;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "163")
public class Nekrataal extends Card {

    public Nekrataal() {
        setNeedsTarget(true);
        setTargetFilter(new NonArtifactNonColorCreatureTargetFilter(Set.of(CardColor.BLACK)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect(Set.of(CardType.CREATURE), true));
    }
}
