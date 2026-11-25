package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "JOU", collectorNumber = "107")
@CardRegistration(set = "FUT", collectorNumber = "105")
@CardRegistration(set = "TSR", collectorNumber = "183")
public class RiddleOfLightning extends Card {

    public RiddleOfLightning() {
        addSpellEffect(new ScryEffect(3));
        addSpellEffect(new RevealTopCardOfLibraryEffect(Subjects.you()));
        addSpellEffect(DealDamageEffect.toAnyTarget(AmountThisWay.revealedManaValue()));
    }
}
