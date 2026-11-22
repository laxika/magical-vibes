package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "JOU", collectorNumber = "43")
public class InterpretTheSigns extends Card {

    public InterpretTheSigns() {
        addSpellEffect(new ScryEffect(3));
        addSpellEffect(new RevealTopCardOfLibraryEffect(Subjects.you()));
        addSpellEffect(new DrawCardEffect(AmountThisWay.revealedManaValue()));
    }
}
