package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "20")
@CardRegistration(set = "ELD", collectorNumber = "340")
public class LindenTheSteadfastQueen extends Card {

    public LindenTheSteadfastQueen() {
        addTrigger(Triggers.creatureYouControlAttacks(),
                new TriggeringCardConditionalEffect(
                        new CardColorPredicate(CardColor.WHITE),
                        new GainLifeEffect(1)));
    }
}
