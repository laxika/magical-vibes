package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulswornSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Soulsworn Spirit cannot be blocked")
    void cannotBeBlocked() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent spirit = new Permanent(new SoulswornSpirit());
        spirit.setSummoningSick(false);
        spirit.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(spirit);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Detained creature can't attack")
    void detainedCreatureCannotAttack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = detain("Grizzly Bears");

        assertThatThrownBy(() -> declareAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Detained creature can't activate its abilities")
    void detainedCreatureCannotActivateAbilities() {
        addCreatureReady(player2, new LlanowarElves());
        detain("Llanowar Elves");

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Detain wears off at the Spirit controller's next turn")
    void detainWearsOffAtControllersNextTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = detain("Grizzly Bears");

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareAttack(bears)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID ownBearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new SoulswornSpirit()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    /** Casts the Spirit targeting the named player2 creature and resolves both spell and trigger. */
    private Permanent detain(String targetName) {
        UUID targetId = harness.getPermanentId(player2, targetName);
        harness.setHand(player1, List.of(new SoulswornSpirit()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        gs.playCard(gd, player1, 0, 0, targetId, null);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
        return gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(targetName))
                .findFirst()
                .orElseThrow();
    }

    /** Attempts to declare the given player2 creature as an attacker. */
    private void declareAttack(Permanent creature) {
        creature.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        gs.declareAttackers(gd, player2, List.of(index));
    }
}
