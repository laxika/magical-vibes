package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "59")
@CardRegistration(set = "MSC", collectorNumber = "370")
public class PuppetMasterStringPuller extends Card {

    public PuppetMasterStringPuller() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new GoadTargetCreatureUntilNextTurnEffect())
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new CantBlockThisTurnEffect(TapUntapScope.TARGET));

        addEffect(EffectSlot.ON_GOADED_CREATURES_COMBAT_DAMAGE_TO_OPPONENT,
                CreateTokenEffect.ofTreasureToken(1));
    }
}
