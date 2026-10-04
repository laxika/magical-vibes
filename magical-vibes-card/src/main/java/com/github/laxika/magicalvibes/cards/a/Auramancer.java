package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "M12", collectorNumber = "9")
@CardRegistration(set = "M14", collectorNumber = "6")
@CardRegistration(set = "ORI", collectorNumber = "5")
@CardRegistration(set = "ODY", collectorNumber = "5")
@CardRegistration(set = "DDL", collectorNumber = "9")
@CardRegistration(set = "PC2", collectorNumber = "2")
@CardRegistration(set = "A25", collectorNumber = "6")
@CardRegistration(set = "PCA", collectorNumber = "2")
@CardRegistration(set = "DMR", collectorNumber = "1")
@CardRegistration(set = "DSC", collectorNumber = "97")
public class Auramancer extends Card {

    public Auramancer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.ENCHANTMENT))
                .targetGraveyard(true)
                .build(), "Return the targeted enchantment card to your hand?"));
    }
}
