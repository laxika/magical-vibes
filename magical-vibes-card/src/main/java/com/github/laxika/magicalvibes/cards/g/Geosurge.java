package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;

import java.util.Set;

@CardRegistration(set = "NPH", collectorNumber = "85")
public class Geosurge extends Card {

    public Geosurge() {
        addEffect(EffectSlot.SPELL, new AwardRestrictedManaEffect(ManaColor.RED, 7, Set.of(CardType.CREATURE, CardType.ARTIFACT)));
    }
}
