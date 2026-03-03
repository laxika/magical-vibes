package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect;

import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "82")
public class LeadTheStampede extends Card {

    public LeadTheStampede() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect(5, Set.of(CardType.CREATURE), true));
    }
}
