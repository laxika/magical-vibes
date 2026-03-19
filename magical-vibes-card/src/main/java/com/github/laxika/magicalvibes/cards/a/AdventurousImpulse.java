package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect;

import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "153")
public class AdventurousImpulse extends Card {

    public AdventurousImpulse() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect(3, Set.of(CardType.CREATURE, CardType.LAND)));
    }
}
