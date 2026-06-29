package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlissaTheTraitorTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_OPPONENT_CREATURE_DIES trigger with MayEffect wrapping ReturnCardFromGraveyardEffect")
    void hasCorrectStructure() {
        GlissaTheTraitor card = new GlissaTheTraitor();

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_DIES).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_DIES).getFirst();
        assertThat(may.wrapped()).isInstanceOf(ReturnCardFromGraveyardEffect.class);
    }

    // ===== Trigger fires when opponent's creature dies =====

    @Test
    @DisplayName("Triggers may ability when opponent's creature dies")
    void triggersWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new GlissaTheTraitor());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));

        // Player1 casts Cruel Edict targeting player2 → opponent's creature dies
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());

        harness.passBothPriorities(); // Resolve Cruel Edict

        // Glissa's MayEffect goes on stack — resolve it to get prompt
        harness.passBothPriorities();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    // ===== Does NOT trigger when own creature dies =====

    @Test
    @DisplayName("Does not trigger when controller's own creature dies")
    void doesNotTriggerWhenOwnCreatureDies() {
        harness.addToBattlefield(player1, new GlissaTheTraitor());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());

        harness.passBothPriorities(); // Resolve Cruel Edict → player1's creature dies

        // Glissa should NOT trigger — own creature died
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Glissa, the Traitor"));
    }

    // ===== Accepting may ability and returning artifact =====

    @Test
    @DisplayName("Accepting may ability and choosing artifact returns it from graveyard to hand")
    void acceptingReturnsArtifactToHand() {
        harness.addToBattlefield(player1, new GlissaTheTraitor());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());

        harness.passBothPriorities(); // Resolve Cruel Edict → trigger
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true); // Accept — inner resolves inline → graveyard choice

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        harness.handleGraveyardCardChosen(player1, 0); // Choose Leonin Scimitar

        harness.assertInHand(player1, "Leonin Scimitar");
        harness.assertNotInGraveyard(player1, "Leonin Scimitar");
    }

    // ===== Declining may ability =====

    @Test
    @DisplayName("Declining may ability does not return artifact")
    void decliningDoesNotReturnArtifact() {
        harness.addToBattlefield(player1, new GlissaTheTraitor());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());

        harness.passBothPriorities(); // Resolve Cruel Edict → trigger
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, false); // Decline

        // Artifact stays in graveyard
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertNotInHand(player1, "Leonin Scimitar");
    }

    // ===== No artifact in graveyard =====

    @Test
    @DisplayName("No effect when graveyard has no artifact cards")
    void noEffectWithNoArtifactsInGraveyard() {
        harness.addToBattlefield(player1, new GlissaTheTraitor());
        harness.addToBattlefield(player2, new GrizzlyBears());
        // No artifacts in graveyard

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());

        harness.passBothPriorities(); // Resolve Cruel Edict → trigger
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true); // Accept — inner resolves inline → no artifacts

        // Should resolve with no effect (no graveyard choice prompt)
        assertThat(gd.gameLog).anyMatch(s -> s.contains("no artifact cards in graveyard"));
    }

    // ===== Opponent creature killed by damage =====

    @Test
    @DisplayName("Triggers when opponent's creature is killed by damage spell")
    void triggersWhenOpponentCreatureKilledByDamage() {
        harness.addToBattlefield(player1, new GlissaTheTraitor());
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve first Shock (2 damage kills 2/2)

        // Grizzly Bears dies → Glissa's MayEffect goes on stack — resolve it to get prompt
        harness.passBothPriorities();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    // ===== Helper methods =====

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
