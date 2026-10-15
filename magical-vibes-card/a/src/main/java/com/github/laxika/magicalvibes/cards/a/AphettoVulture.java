package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "ONS", collectorNumber = "126")
public class AphettoVulture extends Card {

    public AphettoVulture() {
        addEffect(EffectSlot.ON_DEATH, new MayEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY)
                .filter(new CardSubtypePredicate(CardSubtype.ZOMBIE))
                .targetGraveyard(true)
                .build(), "Put the targeted Zombie card on top of its owner's library?"));
    }
}
