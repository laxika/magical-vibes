package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RealityRipple;
import com.github.laxika.magicalvibes.cards.w.WildJhovall;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SandSquid.class, WildJhovall.class, Forest.class, Island.class, RealityRipple.class})
class SandSquidTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability taps the target creature")
    void resolvingAbilityTapsTargetCreature() {
        addCreatureReady(player1, new SandSquid());
        Permanent targetCreature = addCreatureReady(player2, new WildJhovall());

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(targetCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new SandSquid());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The target creature remains tapped while Sand Squid remains tapped")
    void targetCreatureDoesNotUntapWhileSandSquidRemainsTapped() {
        Permanent sandSquid = addCreatureReady(player1, new SandSquid());
        Permanent targetCreature = addCreatureReady(player2, new WildJhovall());

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();
        advanceToNextTurn(player1);

        assertThat(sandSquid.isTapped()).isTrue();
        assertThat(targetCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The target creature untaps after Sand Squid untaps")
    void targetCreatureUntapsAfterSandSquidUntaps() {
        Permanent sandSquid = addCreatureReady(player1, new SandSquid());
        Permanent targetCreature = addCreatureReady(player2, new WildJhovall());

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, true);
        advanceToNextTurn(player1);

        assertThat(sandSquid.isTapped()).isFalse();
        assertThat(targetCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The target creature remains locked when the controller declines to untap Sand Squid")
    void targetCreatureRemainsLockedWhenSandSquidStaysTapped() {
        Permanent sandSquid = addCreatureReady(player1, new SandSquid());
        Permanent targetCreature = addCreatureReady(player2, new WildJhovall());

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();
        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, false);
        advanceToNextTurn(player1);

        assertThat(sandSquid.isTapped()).isTrue();
        assertThat(targetCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The target creature untaps after Sand Squid leaves the battlefield")
    void targetCreatureUntapsAfterSandSquidLeavesBattlefield() {
        Permanent sandSquid = addCreatureReady(player1, new SandSquid());
        Permanent targetCreature = addCreatureReady(player2, new WildJhovall());

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(sandSquid);
        advanceToNextTurn(player1);

        assertThat(targetCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The controller may choose not to untap Sand Squid")
    void mayChooseNotToUntap() {
        Permanent sandSquid = addCreatureReady(player1, new SandSquid());
        sandSquid.tap();

        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(sandSquid.isTapped()).isTrue();
    }

    @Test
    void alreadyTappedCreatureIsStillLocked() {
        addCreatureReady(player1, new SandSquid());
        Permanent target = addCreatureReady(player2, new WildJhovall());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        advanceToNextTurn(player1);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void abilityStillTapsTargetWhenSourceLeavesBeforeResolution() {
        Permanent source = addCreatureReady(player1, new SandSquid());
        Permanent target = addCreatureReady(player2, new WildJhovall());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        advanceToNextTurn(player1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void untappingAndRetappingSourceBeforeResolutionDoesNotCreateLock() {
        Permanent source = addCreatureReady(player1, new SandSquid());
        Permanent target = addCreatureReady(player2, new WildJhovall());

        harness.activateAbility(player1, 0, null, target.getId());
        source.untap();
        source.tap();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        advanceToNextTurn(player1);
        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void targetingItselfPreventsItsOwnUntap() {
        Permanent source = addCreatureReady(player1, new SandSquid());

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();
        advanceToNextTurn(player2);

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void islandwalkPreventsBlockingWhenDefenderControlsIsland() {
        Permanent attacker = addCreatureReady(player1, new SandSquid());
        Permanent blocker = addCreatureReady(player2, new WildJhovall());
        harness.addToBattlefield(player2, new Island());

        assertThat(harness.getBlockLegalityService().canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void islandOnAttackersSideDoesNotPreventBlocking() {
        Permanent attacker = addCreatureReady(player1, new SandSquid());
        Permanent blocker = addCreatureReady(player2, new WildJhovall());
        harness.addToBattlefield(player1, new Island());

        assertThat(harness.getBlockLegalityService().canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void phasingOutSourcePermanentlyEndsUntapLock() {
        Permanent source = addCreatureReady(player1, new SandSquid());
        Permanent target = addCreatureReady(player2, new WildJhovall());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        advanceToNextTurn(player1);
        assertThat(target.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, List.of(new RealityRipple()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);

        advanceToNextTurnWithMayChoice(player2, false);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(source.isTapped()).isTrue();
        advanceToNextTurn(player1);

        assertThat(target.isTapped()).isFalse();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(newActivePlayer, TurnStep.UPKEEP);
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
