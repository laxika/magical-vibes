package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesOfSubtypeThisTurn;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.ExileRandomNonlandCardFromDamagedPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "42")
public class RahildaWantedCutthroat extends Card {

    public RahildaWantedCutthroat() {
        setBackFaceCard(new RahildaFeralOutlaw());
        addAbilities();
    }

    @Override
    public String getBackFaceClassName() {
        return "RahildaFeralOutlaw";
    }

    static Condition duringTurnAttackedWithWolfOrWerewolf() {
        return new AnyOf(List.of(
                new AttackedWithCreaturesOfSubtypeThisTurn(1, CardSubtype.WOLF),
                new AttackedWithCreaturesOfSubtypeThisTurn(1, CardSubtype.WEREWOLF)));
    }

    private void addAbilities() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileRandomNonlandCardFromDamagedPlayerLibraryEffect(duringTurnAttackedWithWolfOrWerewolf()));
    }
}
