package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "136")
@CardRegistration(set = "M12", collectorNumber = "94")
@CardRegistration(set = "CHK", collectorNumber = "111")
public class Distress extends Card {

    public Distress() {
        addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandEffect(1, List.of(CardType.LAND), HandChoiceDestination.DISCARD));
    }
}
