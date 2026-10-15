package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.s.ShelteringLight;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrightReprisal.class, RaptorCompanion.class, ShelteringLight.class})
class BrightReprisalTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Bright Reprisal targeting an attacking creature puts it on the stack")
    void castingTargetingAttackingCreature() {
        Permanent attacker = addCreatureReady(player1, new RaptorCompanion());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BrightReprisal()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(BrightReprisal.class);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent attacker = addCreatureReady(player2, new RaptorCompanion());
        attacker.setAttacking(true);

        harness.addToBattlefield(player1, new RaptorCompanion());
        UUID targetId = harness.getPermanentId(player1, "Raptor Companion");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BrightReprisal()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Resolving destroys the attacking creature and draws a card")
    void resolvingDestroysAttackingCreatureAndDrawsCard() {
        Permanent attacker = addCreatureReady(player1, new RaptorCompanion());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BrightReprisal()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        int handSizeAfterCast = harness.getGameData().playerHands.get(player2.getId()).size();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature destroyed
        harness.assertNotOnBattlefield(player1, "Raptor Companion");
        harness.assertInGraveyard(player1, "Raptor Companion");
        // Card drawn
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeAfterCast + 1);
    }

    @Test
    @DisplayName("Bright Reprisal goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent attacker = addCreatureReady(player1, new RaptorCompanion());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BrightReprisal()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Bright Reprisal");
    }

    @Test
    @DisplayName("Bright Reprisal fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent attacker = addCreatureReady(player1, new RaptorCompanion());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BrightReprisal()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        int handSizeAfterCast = harness.getGameData().playerHands.get(player2.getId()).size();

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gameLogContains("fizzles")).isTrue();
        // Bright Reprisal still goes to graveyard
        harness.assertInGraveyard(player2, "Bright Reprisal");
        // No card drawn when fizzled
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeAfterCast);
    }

    @Test
    @DisplayName("No card is drawn if the target stops attacking before resolution")
    void targetStopsAttackingBeforeResolution() {
        Permanent attacker = addCreatureReady(player1, new RaptorCompanion());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BrightReprisal()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.setLibrary(player2, List.of(new RaptorCompanion()));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raptor Companion");
        harness.assertInGraveyard(player2, "Bright Reprisal");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws a card even when indestructible prevents destruction")
    void drawsWhenDestructionIsPrevented() {
        Permanent attacker = addCreatureReady(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new ShelteringLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BrightReprisal()));
        harness.setLibrary(player2, List.of(new RaptorCompanion()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raptor Companion");
        harness.assertNotInGraveyard(player1, "Raptor Companion");
        harness.assertInHand(player2, "Raptor Companion");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Bright Reprisal");
    }
}
