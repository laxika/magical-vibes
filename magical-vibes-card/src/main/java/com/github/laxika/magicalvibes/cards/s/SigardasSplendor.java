package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfControllerLifeTotalAtLeastNotedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.NoteControllerLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "33")
public class SigardasSplendor extends Card {

    public SigardasSplendor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new NoteControllerLifeTotalEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DrawCardIfControllerLifeTotalAtLeastNotedEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.WHITE), List.of(new GainLifeEffect(1))));
    }
}
