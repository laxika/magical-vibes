package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEnteringCreatureEffect;

import java.util.Set;

@CardRegistration(set = "M14", collectorNumber = "148")
public class OgreBattledriver extends Card {

    public OgreBattledriver() {
        // Whenever another creature you control enters, that creature gets +2/+0 and gains haste until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new BoostEnteringCreatureEffect(2, 0, Set.of(Keyword.HASTE)));
    }
}
