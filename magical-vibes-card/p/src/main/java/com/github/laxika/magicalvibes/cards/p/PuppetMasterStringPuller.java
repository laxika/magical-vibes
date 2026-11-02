package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "59")
@CardRegistration(set = "MSC", collectorNumber = "370")
public class PuppetMasterStringPuller extends Card {

    public PuppetMasterStringPuller() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.youAttack(),
                        new GoadTargetCreatureUntilNextTurnEffect())
                .addTrigger(Triggers.youAttack(),
                        new CantBlockThisTurnEffect(TapUntapScope.TARGET));

        addTrigger(Triggers.goadedCreaturesDealCombatDamageToOpponent(),
                CreateTokenEffect.ofTreasureToken(1));
    }
}
