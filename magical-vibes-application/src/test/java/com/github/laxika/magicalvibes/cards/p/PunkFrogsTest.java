package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PunkFrogs.class, Shock.class})
class PunkFrogsTest extends BaseCardTest {

    @Test
    @DisplayName("Ward counters an opponent's spell when its controller does not pay")
    void wardCountersUnpaidSpell() {
        Permanent frogs = harness.addToBattlefieldAndReturn(player1, new PunkFrogs());
        castOpponentShock(frogs, 1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Punk Frogs");
    }

    @Test
    @DisplayName("Paying ward lets an opponent's spell resolve")
    void payingWardLetsSpellResolve() {
        Permanent frogs = harness.addToBattlefieldAndReturn(player1, new PunkFrogs());
        castOpponentShock(frogs, 3);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(frogs);
    }

    private void castOpponentShock(Permanent target, int wardMana) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, wardMana);
        harness.castInstant(player2, 0, target.getId());
    }
}
