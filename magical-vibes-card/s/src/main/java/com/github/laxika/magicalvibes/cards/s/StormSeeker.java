package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "CHR", collectorNumber = "70")
@CardRegistration(set = "LEG", collectorNumber = "205")
@CardRegistration(set = "ME1", collectorNumber = "132")
public class StormSeeker extends Card {

    public StormSeeker() {
        addSpellEffect(DealDamageEffect.to(Subjects.targetPlayer(), new CardsInHand(CountScope.TARGET_PLAYER)));
    }
}
