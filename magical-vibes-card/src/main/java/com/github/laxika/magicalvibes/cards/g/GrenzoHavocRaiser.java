package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerCastPermissionUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GoadChosenCreatureDamagedPlayerControlsEffect;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "228")
@CardRegistration(set = "CMM", collectorNumber = "538")
@CardRegistration(set = "CMM", collectorNumber = "677")
public class GrenzoHavocRaiser extends Card {

    public GrenzoHavocRaiser() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Goad target creature that player controls",
                        new GoadChosenCreatureDamagedPlayerControlsEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile the top card of that player's library. Until end of turn, you may cast that card "
                                + "and you may spend mana as though it were mana of any color to cast that spell.",
                        new ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerCastPermissionUntilEndOfTurnEffect(
                                true))
        )));
    }
}
