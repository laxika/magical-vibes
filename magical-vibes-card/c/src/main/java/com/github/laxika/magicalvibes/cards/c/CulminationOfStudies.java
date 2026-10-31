package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndApplyColorRidersEffect;

@CardRegistration(set = "STX", collectorNumber = "173")
public class CulminationOfStudies extends Card {

    public CulminationOfStudies() {
        addSpellEffect(new ExileTopCardsAndApplyColorRidersEffect(new XValue()));
    }
}
