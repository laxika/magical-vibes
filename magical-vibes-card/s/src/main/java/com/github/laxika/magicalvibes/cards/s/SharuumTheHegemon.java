package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ALA", collectorNumber = "194")
@CardRegistration(set = "V11", collectorNumber = "11")
@CardRegistration(set = "2XM", collectorNumber = "219")
@CardRegistration(set = "C13", collectorNumber = "212")
@CardRegistration(set = "C16", collectorNumber = "221")
@CardRegistration(set = "BRC", collectorNumber = "128")
public class SharuumTheHegemon extends Card {

    public SharuumTheHegemon() {
        // "When Sharuum enters, you may return target artifact card from your graveyard to the
        // battlefield." The target is chosen when the trigger goes on the stack (CR 603.3d); the
        // "may" is decided on resolution (CR 603.5).
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.ARTIFACT))
                .targetGraveyard(true)
                .build(), "Return the targeted artifact card to the battlefield?"));
    }
}
