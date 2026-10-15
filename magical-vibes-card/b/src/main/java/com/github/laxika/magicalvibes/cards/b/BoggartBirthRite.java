package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "101")
public class BoggartBirthRite extends Card {

    public BoggartBirthRite() {
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder().destination(GraveyardChoiceDestination.HAND).filter(new CardSubtypePredicate(CardSubtype.GOBLIN)).targetGraveyard(true).build());
    }
}
