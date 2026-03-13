package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "M10", collectorNumber = "92")
@CardRegistration(set = "M11", collectorNumber = "94")
public class Disentomb extends Card {

    public Disentomb() {
        addEffect(EffectSlot.SPELL, new ReturnCardFromGraveyardEffect(
                GraveyardChoiceDestination.HAND, new CardTypePredicate(CardType.CREATURE), true));
    }
}
