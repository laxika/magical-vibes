package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SerpentAssassinTest extends BaseCardTest {

    /**
     * Casts Serpent Assassin, resolves it onto the battlefield, accepts the "may" ability and
     * chooses the target creature so the ETB triggered ability is placed on the stack.
     */
    private void castAndAcceptMay(UUID creatureId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SerpentAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> target choice
        harness.handlePermanentChosen(player1, creatureId);
        harness.passBothPriorities(); // resolve triggered ability -> may prompt
        harness.handleMayAbilityChosen(player1, true);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Serpent Assassin puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SerpentAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Serpent Assassin");
    }

    @Test
    @DisplayName("Resolving puts Serpent Assassin on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new SerpentAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serpent Assassin");
    }

    // ===== ETB may ability =====

    @Test
    @DisplayName("Resolving Serpent Assassin triggers may prompt when a nonblack creature exists")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SerpentAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> target choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Accepting may and choosing target destroys the nonblack creature")
    void acceptingMayDestroysTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndAcceptMay(targetId);

        // Resolve ETB triggered ability
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the may ability does not destroy the creature")
    void decliningMaySkipsDestruction() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SerpentAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> target choice
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities(); // resolve triggered ability -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Serpent Assassin");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("May prompt does not fire when only black creatures exist")
    void noMayPromptWhenOnlyBlackCreatures() {
        harness.addToBattlefield(player2, new MassOfGhouls());
        harness.setHand(player1, List.of(new SerpentAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> enters battlefield

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Mass of Ghouls");
    }

    // ===== No target scenarios =====

    @Test
    @DisplayName("May prompt does not fire when no creatures exist")
    void noMayPromptWhenNoCreatures() {
        harness.setHand(player1, List.of(new SerpentAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> enters battlefield

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Serpent Assassin");
    }
}
