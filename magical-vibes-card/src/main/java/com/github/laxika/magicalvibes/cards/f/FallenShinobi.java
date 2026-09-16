package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffect;

@CardRegistration(set = "MH1", collectorNumber = "199")
@CardRegistration(set = "AA3", collectorNumber = "19")
public class FallenShinobi extends Card {

    public FallenShinobi() {
        addNinjutsu("{2}{U}{B}");
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffect(
                        2, true));
    }
}
