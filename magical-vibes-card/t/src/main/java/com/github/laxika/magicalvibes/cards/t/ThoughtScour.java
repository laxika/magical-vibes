package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "DKA", collectorNumber = "52")
@CardRegistration(set = "DDM", collectorNumber = "21")
@CardRegistration(set = "IMA", collectorNumber = "76")
@CardRegistration(set = "2X2", collectorNumber = "65")
@CardRegistration(set = "SLZ", collectorNumber = "31")
@CardRegistration(set = "SLZ", collectorNumber = "152")
@CardRegistration(set = "SLZ", collectorNumber = "273")
@CardRegistration(set = "AA2", collectorNumber = "5")
@CardRegistration(set = "SLD", collectorNumber = "2479")
public class ThoughtScour extends Card {

    public ThoughtScour() {
        addSpellEffect(new MillEffect(2, MillRecipient.TARGET_PLAYER));
        addSpellEffect(new DrawCardEffect(1));
    }
}
