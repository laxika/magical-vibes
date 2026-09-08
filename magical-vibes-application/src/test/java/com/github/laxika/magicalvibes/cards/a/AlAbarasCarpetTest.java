package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({AlAbarasCarpet.class, HillGiant.class, AirElemental.class})
class AlAbarasCarpetTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from non-flying attackers but not flying attackers")
    void preventsDamageFromNonFlyingAttackersOnly() {
        Permanent carpet = harness.addToBattlefieldAndReturn(player1, new AlAbarasCarpet());
        addCreatureReady(player2, new HillGiant());
        addCreatureReady(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(carpet), null, null);
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Prevention expires at end of turn")
    void preventionExpiresAtEndOfTurn() {
        Permanent carpet = harness.addToBattlefieldAndReturn(player1, new AlAbarasCarpet());
        addCreatureReady(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(carpet), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 17);
    }
}
