package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "217")
@CardRegistration(set = "BRR", collectorNumber = "19")
@CardRegistration(set = "MSC", collectorNumber = "200")
@CardRegistration(set = "MSC", collectorNumber = "437")
@CardRegistration(set = "BLC", collectorNumber = "276")
public class HelmOfTheHost extends Card {

    public HelmOfTheHost() {
        // At the beginning of combat on your turn, create a token that's a copy of
        // equipped creature, except the token isn't legendary. That token gains haste.
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new CreateTokenCopyOfEquippedCreatureEffect(true, true));

        // Equip {5}
        addActivatedAbility(new EquipActivatedAbility("{5}"));
    }
}
