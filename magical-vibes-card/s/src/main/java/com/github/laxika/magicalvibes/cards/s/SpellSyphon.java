package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import java.util.EnumSet;

@CardRegistration(set = "SHM", collectorNumber = "52")
public class SpellSyphon extends Card {

    public SpellSyphon() {
        // Counter target spell unless its controller pays {1} for each blue permanent you control.
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(
                new PermanentCount(new PermanentColorInPredicate(EnumSet.of(CardColor.BLUE)), CountScope.CONTROLLER)));
    }
}
