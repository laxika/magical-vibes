package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameToHandEffect;

@CardRegistration(set = "JUD", collectorNumber = "64")
public class DeathWish extends Card {

    public DeathWish() {
        addSpellEffect(new SearchOutsideGameToHandEffect());
        addSpellEffect(new LoseLifeEffect(
                new HalvedRoundedUp(new ControllerLifeTotal()), LoseLifeRecipient.CONTROLLER));
        addSpellEffect(new ExileSpellEffect());
    }
}
