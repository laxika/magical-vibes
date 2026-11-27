package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardsCantEnterBattlefieldFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndReturnAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "569")
public class BadWolfBay extends Card {

    public BadWolfBay() {
        target(TargetFilters.creature(), 0, 1).addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new ExileTargetCreatureAndReturnAtNextEndStepEffect());
        // "When chaos ensues, cards can't enter from exile this turn. Then planeswalk." is one ability.
        addTrigger(Triggers.chaosEnsues(), GrantStaticEffectToPlayerEffect.untilEndOfTurn(
                new CardsCantEnterBattlefieldFromZonesEffect(null, Set.of(Zone.EXILE))), new PlaneswalkEffect());
    }
}
