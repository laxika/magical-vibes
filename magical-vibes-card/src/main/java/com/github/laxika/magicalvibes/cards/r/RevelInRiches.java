package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlsPermanentCountConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "XLN", collectorNumber = "117")
public class RevelInRiches extends Card {

    public RevelInRiches() {
        // Whenever a creature an opponent controls dies, create a Treasure token.
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, CreateTokenEffect.ofTreasureToken(1));

        // At the beginning of your upkeep, if you control ten or more Treasures, you win the game.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ControlsPermanentCountConditionalEffect(10,
                        new PermanentHasSubtypePredicate(CardSubtype.TREASURE),
                        new WinGameEffect()));
    }
}
