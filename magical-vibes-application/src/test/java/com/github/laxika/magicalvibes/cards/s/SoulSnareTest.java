package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulSnare.class, GrizzlyBears.class, JaceBeleren.class})
class SoulSnareTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature attacking its controller")
    void exilesCreatureAttackingYou() {
        Permanent snare = addSnare();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        declareAttack(player2, attacker, null);

        activateSnare(snare, attacker);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiles a creature attacking a planeswalker it controls")
    void exilesCreatureAttackingYourPlaneswalker() {
        Permanent snare = addSnare();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        declareAttack(player2, attacker, planeswalker);

        activateSnare(snare, attacker);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent snare = addSnare();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(snare),
                null,
                creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Soul Snare");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrifices Soul Snare as a cost before exiling the attacker")
    void sacrificesSourceBeforeResolution() {
        Permanent snare = addSnare();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        declareAttack(player2, attacker, null);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(snare), null, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Soul Snare");
        harness.assertInGraveyard(player1, "Soul Snare");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not exile a target that stops attacking before resolution")
    void targetMustStillBeAttackingAtResolution() {
        Permanent snare = addSnare();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        declareAttack(player2, attacker, null);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(snare), null, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Soul Snare");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot exile its controller's creature attacking the opponent")
    void cannotTargetCreatureAttackingOpponent() {
        Permanent snare = addSnare();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttack(player1, attacker, null);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(snare), null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Soul Snare");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without paying white mana")
    void cannotActivateWithoutWhiteMana() {
        Permanent snare = addSnare();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        declareAttack(player2, attacker, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(snare), null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Soul Snare");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not exile an attacker after the attacked planeswalker leaves the battlefield")
    void targetIsIllegalAfterAttackedPlaneswalkerLeaves() {
        Permanent snare = addSnare();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        declareAttack(player2, attacker, planeswalker);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(snare), null, attacker.getId());

        planeswalker.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Jace Beleren");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Soul Snare");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addSnare() {
        return harness.addToBattlefieldAndReturn(player1, new SoulSnare());
    }

    private void activateSnare(Permanent snare, Permanent target) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(snare),
                null,
                target.getId());
        harness.passBothPriorities();
    }

    private void declareAttack(Player attackerPlayer, Permanent attacker, Permanent attackTarget) {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            int attackerIndex = gd.playerBattlefields.get(attackerPlayer.getId()).indexOf(attacker);
            if (attackTarget == null) {
                declareAttackers(attackerPlayer, List.of(attackerIndex));
            } else {
                harness.forceActivePlayer(attackerPlayer);
                harness.forceStep(TurnStep.DECLARE_ATTACKERS);
                harness.clearPriorityPassed();
                harness.beginAttackerDeclarationInput();
                gs.declareAttackers(gd, attackerPlayer, List.of(attackerIndex),
                        Map.of(attackerIndex, attackTarget.getId()));
            }
        });
    }
}
