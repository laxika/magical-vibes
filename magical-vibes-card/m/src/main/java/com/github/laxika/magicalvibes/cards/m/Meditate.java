package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;

@CardRegistration(set = "TMP", collectorNumber = "76")
@CardRegistration(set = "TPR", collectorNumber = "60")
public class Meditate extends Card {

    public Meditate() {
        // Draw four cards. You skip your next turn.
        addSpellEffect(new DrawCardEffect(4));
        addSpellEffect(new SkipNextEffect(SkipKind.TURN));
    }
}
