package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetRevealsCardsControllerChoosesDiscardEffect;

@CardRegistration(set = "9ED", collectorNumber = "115")
public class Blackmail extends Card {

    public Blackmail() {
        addEffect(EffectSlot.SPELL, new TargetRevealsCardsControllerChoosesDiscardEffect(3));
    }
}
