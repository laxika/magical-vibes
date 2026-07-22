package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThermoAlchemistTest extends BaseCardTest {

    // ===== Activated ability: {T}: deal 1 damage to each opponent =====

    @Test
    @DisplayName("Tap ability deals 1 damage to each opponent")
    void tapAbilityDealsDamage() {
        addAlchemistReady(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Tap ability taps the creature")
    void tapAbilityTapsCreature() {
        Permanent perm = addAlchemistReady(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(perm.isTapped()).isTrue();
    }

    // ===== Instant/sorcery cast trigger untaps =====

    @Test
    @DisplayName("Casting an instant triggers untap")
    void instantSpellTriggersUntap() {
        addAlchemistReady(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Thermo-Alchemist"));
    }

    @Test
    @DisplayName("Resolving instant cast trigger untaps Thermo-Alchemist")
    void instantTriggerUntaps() {
        Permanent perm = addAlchemistReady(player1);
        perm.tap();
        assertThat(perm.isTapped()).isTrue();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(perm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a creature does not trigger untap")
    void creatureDoesNotTrigger() {
        addAlchemistReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting an instant does not trigger controller's Thermo-Alchemist")
    void opponentInstantDoesNotTrigger() {
        addAlchemistReady(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    // ===== Tap + untap interaction =====

    @Test
    @DisplayName("Can tap for damage, then cast instant to untap and tap again")
    void tapUntapTapAgain() {
        Permanent perm = addAlchemistReady(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        assertThat(perm.isTapped()).isTrue();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(perm.isTapped()).isFalse();

        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        // 20 −1 (tap) −2 (Shock) −1 (tap) = 16
        harness.assertLife(player2, 16);
    }

    // ===== Helpers =====

    private Permanent addAlchemistReady(Player player) {
        Permanent perm = new Permanent(new ThermoAlchemist());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
