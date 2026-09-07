package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcumsSleigh.class, BalduvianBears.class, Island.class, SnowCoveredIsland.class})
class ArcumsSleighTest extends BaseCardTest {

    @Test
    @DisplayName("Grants vigilance during combat while the defending player controls a snow land")
    void grantsVigilanceDuringCombat() {
        harness.addToBattlefield(player2, new SnowCoveredIsland());
        Permanent sleigh = harness.addToBattlefieldAndReturn(player1, new ArcumsSleigh());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        enterCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, sleigh), 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(sleigh.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new SnowCoveredIsland());
        Permanent sleigh = harness.addToBattlefieldAndReturn(player1, new ArcumsSleigh());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        enterCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, sleigh), 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate outside combat")
    void cannotActivateOutsideCombat() {
        harness.addToBattlefield(player2, new SnowCoveredIsland());
        Permanent sleigh = harness.addToBattlefieldAndReturn(player1, new ArcumsSleigh());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, sleigh), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when the defending player controls no snow land")
    void cannotActivateWithoutSnowLand() {
        harness.addToBattlefield(player2, new Island());
        Permanent sleigh = harness.addToBattlefieldAndReturn(player1, new ArcumsSleigh());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        enterCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, sleigh), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted vigilance expires at end of turn")
    void grantedVigilanceExpiresAtEndOfTurn() {
        harness.addToBattlefield(player2, new SnowCoveredIsland());
        Permanent sleigh = harness.addToBattlefieldAndReturn(player1, new ArcumsSleigh());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        enterCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, sleigh), 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TurnCleanupService.class)
                .applyCleanupResets(gd));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new SnowCoveredIsland());
        Permanent sleigh = harness.addToBattlefieldAndReturn(player1, new ArcumsSleigh());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        enterCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, sleigh), 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Checks the actual defending player when its controller is defending")
    void checksActualDefendingPlayer() {
        Permanent sleigh = harness.addToBattlefieldAndReturn(player1, new ArcumsSleigh());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.addToBattlefield(player1, new SnowCoveredIsland());

        enterOpponentCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, sleigh), 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not use the attacking player's snow land as the defending player's land")
    void ignoresSnowLandControlledByAttackingPlayer() {
        Permanent sleigh = harness.addToBattlefieldAndReturn(player1, new ArcumsSleigh());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.addToBattlefield(player2, new SnowCoveredIsland());

        enterOpponentCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, sleigh), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void enterCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
    }

    private void enterOpponentCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
