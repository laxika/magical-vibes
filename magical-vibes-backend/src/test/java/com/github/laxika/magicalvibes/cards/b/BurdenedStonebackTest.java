package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BurdenedStonebackTest extends BaseCardTest {


    // ===== Card properties =====

    @Test
    @DisplayName("Burdened Stoneback has correct card properties")
    void hasCorrectProperties() {
        BurdenedStoneback card = new BurdenedStoneback();

        assertThat(card.getActivatedAbilities()).hasSize(1);
    }

    // ===== ETB: enters with two -1/-1 counters =====

    @Test
    @DisplayName("Enters the battlefield with two -1/-1 counters (effectively 2/2)")
    void entersWithTwoMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BurdenedStoneback()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (enters battlefield)
        harness.passBothPriorities(); // resolve ETB effect (puts -1/-1 counters)

        GameData gd = harness.getGameData();
        Permanent stoneback = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Burdened Stoneback"))
                .findFirst().orElseThrow();

        assertThat(stoneback.getMinusOneMinusOneCounters()).isEqualTo(2);
        assertThat(stoneback.getEffectivePower()).isEqualTo(2);
        assertThat(stoneback.getEffectiveToughness()).isEqualTo(2);
    }

    // ===== Activated ability: grant indestructible =====

    @Test
    @DisplayName("Activated ability grants target creature indestructible until end of turn")
    void abilityGrantsIndestructible() {
        Permanent stoneback = addReadyStoneback(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player1.getId()).getLast().setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent bears = harness.getGameQueryService().findPermanentById(gd, bearsId);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Activated ability removes a -1/-1 counter from source")
    void abilityRemovesCounter() {
        Permanent stoneback = addReadyStoneback(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).getLast().setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        // Started with 2, now should have 1
        assertThat(stoneback.getMinusOneMinusOneCounters()).isEqualTo(1);
        assertThat(stoneback.getEffectivePower()).isEqualTo(3);
        assertThat(stoneback.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate ability twice (with two -1/-1 counters)")
    void canActivateTwiceWithTwoCounters() {
        Permanent stoneback = addReadyStoneback(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player1.getId()).getLast().setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // First activation
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(stoneback.getMinusOneMinusOneCounters()).isEqualTo(1);

        // Second activation
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(stoneback.getMinusOneMinusOneCounters()).isEqualTo(0);
        assertThat(stoneback.getEffectivePower()).isEqualTo(4);
        assertThat(stoneback.getEffectiveToughness()).isEqualTo(4);
    }

    // ===== Cannot activate without counters =====

    @Test
    @DisplayName("Cannot activate ability when no counters remain")
    void cannotActivateWithoutCounters() {
        Permanent stoneback = addReadyStoneback(player1);
        stoneback.setMinusOneMinusOneCounters(0);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).getLast().setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No counters");
    }

    // ===== Sorcery speed restriction =====

    @Test
    @DisplayName("Cannot activate ability during combat (sorcery speed only)")
    void cannotActivateDuringCombat() {
        addReadyStoneback(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).getLast().setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Cannot activate ability on opponent's turn (sorcery speed only)")
    void cannotActivateOnOpponentsTurn() {
        addReadyStoneback(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).getLast().setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    // ===== Can target itself =====

    @Test
    @DisplayName("Can target itself to gain indestructible")
    void canTargetSelf() {
        Permanent stoneback = addReadyStoneback(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, stoneback.getId());
        harness.passBothPriorities();

        assertThat(stoneback.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(stoneback.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addReadyStoneback(Player player) {
        BurdenedStoneback card = new BurdenedStoneback();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setMinusOneMinusOneCounters(2);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
