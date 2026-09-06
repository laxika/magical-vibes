package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({HarmoniousGrovestrider.class, Forest.class, Shock.class})
class HarmoniousGrovestriderTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of lands its controller controls")
    void powerToughnessEqualControlledLandCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Permanent grovestrider = harness.addToBattlefieldAndReturn(player1, new HarmoniousGrovestrider());

        assertThat(gqs.getEffectivePower(gd, grovestrider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, grovestrider)).isEqualTo(2);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, grovestrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, grovestrider)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when its controller does not pay")
    void wardCountersUnpaidSpell() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent grovestrider = harness.addToBattlefieldAndReturn(player1, new HarmoniousGrovestrider());
        prepareOpponentShock(grovestrider, 1);

        harness.castInstant(player2, 0, grovestrider.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Harmonious Grovestrider");
    }

    @Test
    @DisplayName("Ward lets an opponent's spell resolve when its controller pays")
    void payingWardLetsSpellResolve() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent grovestrider = harness.addToBattlefieldAndReturn(player1, new HarmoniousGrovestrider());
        prepareOpponentShock(grovestrider, 3);

        harness.castInstant(player2, 0, grovestrider.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(grovestrider);
        harness.assertInGraveyard(player2, "Shock");
    }

    private void prepareOpponentShock(Permanent target, int redMana) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, redMana);
    }
}
