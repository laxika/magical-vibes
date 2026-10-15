package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.ThinkingCap;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenthicCriminologists.class, Forest.class, ThinkingCap.class})
class BenthicCriminologistsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may sacrifice an artifact to draw a card")
    void etbSacrificeArtifactDrawsCard() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ThinkingCap());
        harness.setLibrary(player1, List.of(new Forest()));
        castBenthicCriminologists();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInGraveyard(player1, "Thinking Cap");
    }

    @Test
    @DisplayName("Declining the ETB sacrifice draws no card")
    void decliningEtbSacrificeDoesNothing() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ThinkingCap());
        harness.setLibrary(player1, List.of(new Forest()));
        castBenthicCriminologists();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Attacking may sacrifice an artifact to draw a card")
    void attackSacrificeArtifactDrawsCard() {
        addCreatureReady(player1, new BenthicCriminologists());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ThinkingCap());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInGraveyard(player1, "Thinking Cap");
    }

    @Test
    @DisplayName("The ability cannot sacrifice a nonartifact permanent")
    void nonArtifactCannotBeSacrificed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BenthicCriminologists());
        harness.setLibrary(player1, List.of(new Forest()));
        castBenthicCriminologists();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Drawing happens during the sacrifice ability's resolution")
    void drawsBeforePlayersReceivePriorityAfterSacrifice() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ThinkingCap());
        harness.setLibrary(player1, List.of(new Forest()));
        castBenthicCriminologists();
        harness.handleMayAbilityChosen(player1, true);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.handlePermanentChosen(player1, artifact.getId()));

        harness.assertInGraveyard(player1, "Thinking Cap");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay for the draw")
    void opponentArtifactCannotBeSacrificed() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ThinkingCap());
        harness.setLibrary(player1, List.of(new Forest()));
        castBenthicCriminologists();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the attack sacrifice preserves the artifact and draws nothing")
    void decliningAttackSacrificeDoesNothing() {
        addCreatureReady(player1, new BenthicCriminologists());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ThinkingCap());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    private void castBenthicCriminologists() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BenthicCriminologists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
