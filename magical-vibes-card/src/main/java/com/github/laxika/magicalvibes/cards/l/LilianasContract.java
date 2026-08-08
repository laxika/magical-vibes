package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentsWithDifferentNames;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "M19", collectorNumber = "107")
public class LilianasContract extends Card {

    public LilianasContract() {
        // When this enchantment enters, you draw four cards and you lose 4 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(4));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(4));

        // At the beginning of your upkeep, if you control four or more Demons with different names,
        // you win the game.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new ControlsPermanentsWithDifferentNames(4,
                                new PermanentHasSubtypePredicate(CardSubtype.DEMON)),
                        new WinGameEffect()));
    }
}
