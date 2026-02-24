package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutImprintedCreatureOntoBattlefieldEffect;

@CardRegistration(set = "SOM", collectorNumber = "143")
public class CloneShell extends Card {

    public CloneShell() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ImprintFromTopCardsEffect(4));
        addEffect(EffectSlot.ON_DEATH, new PutImprintedCreatureOntoBattlefieldEffect());
    }
}
