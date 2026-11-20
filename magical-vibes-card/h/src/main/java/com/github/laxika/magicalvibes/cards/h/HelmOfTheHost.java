package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
                new CreateTokenCopyOfTargetPermanentEffect(List.of(), Set.of(), null, null, Map.of(), true,
                        false, false, false, false, false, null, Set.of(), true).ofAttached());

        // Equip {5}
        addActivatedAbility(new EquipActivatedAbility("{5}"));
    }
}
