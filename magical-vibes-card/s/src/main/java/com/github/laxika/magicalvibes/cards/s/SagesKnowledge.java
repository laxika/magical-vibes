package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "PTK", collectorNumber = "52")
public class SagesKnowledge extends Card {

    public SagesKnowledge() {
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder().destination(GraveyardChoiceDestination.HAND).filter(new CardTypePredicate(CardType.SORCERY)).targetGraveyard(true).build());
    }
}
