package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "115")
public class InquisitionOfKozilek extends Card {

    public InquisitionOfKozilek() {
        addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandEffect(
                1, List.of(CardType.LAND), new CardMaxManaValuePredicate(3), HandChoiceDestination.DISCARD));
    }
}
