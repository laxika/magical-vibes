package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.RevealUntilTypeMillAndBoostAttackerEffect;

import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "235")
public class TrepanationBlade extends Card {

    public TrepanationBlade() {
        // Whenever equipped creature attacks, defending player reveals cards from the top of
        // their library until they reveal a land card. The creature gets +1/+0 until end of turn
        // for each card revealed this way. That player puts the revealed cards into their graveyard.
        addEffect(EffectSlot.ON_ATTACK,
                new RevealUntilTypeMillAndBoostAttackerEffect(Set.of(CardType.LAND), 1, 0));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
