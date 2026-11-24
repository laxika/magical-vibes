package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PlayerCounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "HML", collectorNumber = "9")
@CardRegistration(set = "ME4", collectorNumber = "18")
public class Leeches extends Card {

    public Leeches() {
        // "Target player loses all poison counters. Leeches deals that much damage to that player."
        addSpellEffect(RemoveAllCountersFromTargetPermanentEffect.fromPlayers(
                Subjects.targetPlayer(), PlayerCounterType.POISON));
        addSpellEffect(new DealDamageToPlayersEffect(new AmountThisWay(StepKind.REMOVE_COUNTERS),
                Subjects.targetPlayerDeclaredElsewhere()));
    }
}
