package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "46")
public class CreepingPeeper extends Card {

    public CreepingPeeper() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.BLUE, 1, new ManaRestriction.EnchantmentOrRoomUnlockOrTurnFaceUp())),
                "{T}: Add {U}. Spend this mana only to cast an enchantment spell, unlock a door, or turn a permanent face up."
        ));
    }
}
