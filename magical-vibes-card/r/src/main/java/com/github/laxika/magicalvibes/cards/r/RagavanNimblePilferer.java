package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerCastPermissionUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandIfDashCostPaidEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "MUL", collectorNumber = "21")
@CardRegistration(set = "MUL", collectorNumber = "86")
@CardRegistration(set = "MUL", collectorNumber = "151")
@CardRegistration(set = "FCA", collectorNumber = "43")
@CardRegistration(set = "MH2", collectorNumber = "138")
public class RagavanNimblePilferer extends Card {

    public RagavanNimblePilferer() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{R}"))));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, SequenceEffect.of(
                CreateTokenEffect.ofTreasureToken(1),
                new ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerCastPermissionUntilEndOfTurnEffect()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnSelfToHandIfDashCostPaidEffect());
    }
}
