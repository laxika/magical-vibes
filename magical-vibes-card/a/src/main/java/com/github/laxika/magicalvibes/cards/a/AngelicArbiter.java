package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantAttackIfCastSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsIfAttackedThisTurnEffect;

@CardRegistration(set = "M11", collectorNumber = "4")
@CardRegistration(set = "CMD", collectorNumber = "5")
@CardRegistration(set = "CMA", collectorNumber = "4")
public class AngelicArbiter extends Card {

    public AngelicArbiter() {
        addStaticEffect(new OpponentsCantAttackIfCastSpellThisTurnEffect());
        addStaticEffect(new OpponentsCantCastSpellsIfAttackedThisTurnEffect());
    }
}
