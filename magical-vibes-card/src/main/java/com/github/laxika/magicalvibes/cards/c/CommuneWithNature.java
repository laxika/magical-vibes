package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "256")
public class CommuneWithNature extends Card {

    public CommuneWithNature() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect(5, Set.of(CardType.CREATURE)));
    }
}
