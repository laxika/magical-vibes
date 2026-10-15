package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThermoAlchemist.class, Shock.class, GrizzlyBears.class, LavaAxe.class})
class ThermoAlchemistTest extends BaseCardTest {

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

        harness.castAndResolveInstant(player1, 0, player2.getId());

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
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(perm.isTapped()).isFalse();

        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        // 20 −1 (tap) −2 (Shock) −1 (tap) = 16
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Sorcery cast trigger untaps before the sorcery resolves")
    void sorceryTriggerUntapsBeforeSpellResolves() {
        Permanent perm = addAlchemistReady(player1);
        perm.tap();
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(perm.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(perm.isTapped()).isFalse();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the tap ability")
    void summoningSicknessPreventsActivation() {
        Permanent perm = addAlchemistReady(player1);
        perm.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(perm.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A tapped Thermo-Alchemist cannot activate again without untapping")
    void tappedCreatureCannotActivateAgain() {
        Permanent perm = addAlchemistReady(player1);
        perm.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each Thermo-Alchemist untaps independently when its controller casts an instant")
    void multipleAlchemistsUntapIndependently() {
        Permanent first = addAlchemistReady(player1);
        Permanent second = addAlchemistReady(player1);
        Permanent opposing = addAlchemistReady(player2);
        first.tap();
        second.tap();
        opposing.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(3);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isTrue();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addAlchemistReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ThermoAlchemist());
        perm.setSummoningSick(false);
        return perm;
    }
}
