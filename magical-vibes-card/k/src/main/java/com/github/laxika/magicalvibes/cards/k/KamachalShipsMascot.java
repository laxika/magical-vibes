package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCardWithManaValueFromDamagedPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "15")
public class KamachalShipsMascot extends Card {

    public KamachalShipsMascot() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new BoostSelfEffect(1, 0)),
                "{R}: Kamachal, Ship's Mascot gets +1/+0 until end of turn."));

        addTrigger(Triggers.dealsCombatDamageToPlayer(), SequenceEffect.of(
                CreateTokenEffect.ofTreasureToken(1),
                new ExileRandomCardWithManaValueFromDamagedPlayerLibraryEffect()));
    }
}
