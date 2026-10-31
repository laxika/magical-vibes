package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "208")
@CardRegistration(set = "P02", collectorNumber = "99")
@CardRegistration(set = "S99", collectorNumber = "104")
public class GoblinLore extends Card {

    public GoblinLore() {
        addSpellEffect(new DrawCardEffect(4));
        addSpellEffect(new DiscardEffect(3, DiscardRecipient.CONTROLLER, true));
    }
}
