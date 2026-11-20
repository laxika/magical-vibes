package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "59")
@CardRegistration(set = "MSC", collectorNumber = "370")
public class PuppetMasterStringPuller extends Card {

    public PuppetMasterStringPuller() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.youAttack(),
                        new GoadTargetCreatureUntilNextTurnEffect(),
                        new CantBlockThisTurnEffect(Subjects.target()));

        addTrigger(Triggers.goadedCreaturesDealCombatDamageToOpponent(),
                CreateTokenEffect.ofTreasureToken(1));
    }
}
