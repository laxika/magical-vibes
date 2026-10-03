package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AffaGuardHound.class})
class AffaGuardHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast during an opponent's turn thanks to flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new AffaGuardHound()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.passPriority(gd, player2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("ETB gives target creature +0/+3 until end of turn")
    void etbBoostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AffaGuardHound());
        harness.setHand(player1, List.of(new AffaGuardHound()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB fizzles if the target creature leaves before resolution")
    void etbFizzlesIfTargetLeaves() {
        harness.addToBattlefield(player2, new AffaGuardHound());
        harness.setHand(player1, List.of(new AffaGuardHound()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Affa Guard Hound");
        harness.castCreature(player1, 0, List.of(targetId));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Can enter an empty battlefield and target itself")
    void canTargetItselfOnEntry() {
        harness.setHand(player1, List.of(new AffaGuardHound()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent hound = findPermanent(player1, "Affa Guard Hound");
        harness.handlePermanentChosen(player1, hound.getId());
        resolveAllTriggers();

        assertThat(hound.getEffectivePower()).isEqualTo(2);
        assertThat(hound.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("ETB resolves even if Affa Guard Hound leaves the battlefield")
    void etbResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AffaGuardHound());
        harness.setHand(player1, List.of(new AffaGuardHound()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();
        resolveAllTriggers();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }
}
