package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaToActivePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "694")
public class KarolinaDeanRunaway extends Card {

    public KarolinaDeanRunaway() {
        // At the beginning of your first main phase, add {W}{U}{B}{R}{G}.
        // This mana can't be spent to cast spells from your hand.
        ManaRestriction restriction = new ManaRestriction.NonHandSpells();
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, new SequenceEffect(List.of(
                new AwardManaToActivePlayerEffect(ManaColor.WHITE, 1, restriction),
                new AwardManaToActivePlayerEffect(ManaColor.BLUE, 1, restriction),
                new AwardManaToActivePlayerEffect(ManaColor.BLACK, 1, restriction),
                new AwardManaToActivePlayerEffect(ManaColor.RED, 1, restriction),
                new AwardManaToActivePlayerEffect(ManaColor.GREEN, 1, restriction))));
    }
}
