package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfControllerLifeTotalAtLeastNotedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.NoteControllerLifeTotalEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "33")
@CardRegistration(set = "DBL", collectorNumber = "33")
public class SigardasSplendor extends Card {

    public SigardasSplendor() {
        addStaticEffect(new NoteControllerLifeTotalEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new DrawCardIfControllerLifeTotalAtLeastNotedEffect());
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.WHITE)), new GainLifeEffect(1));
    }
}
