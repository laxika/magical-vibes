package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenCombatOpponentMatchesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "5ED", collectorNumber = "221")
public class DwarvenSoldier extends Card {

    public DwarvenSoldier() {
        // Whenever this creature blocks or becomes blocked by one or more Orcs,
        // this creature gets +0/+2 until end of turn.
        PermanentHasSubtypePredicate orc = new PermanentHasSubtypePredicate(CardSubtype.ORC);
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfWhenCombatOpponentMatchesEffect(orc, 0, 2));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfWhenCombatOpponentMatchesEffect(orc, 0, 2));
    }
}
