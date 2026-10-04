package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EsotericDuplicator.class, ChromaticStar.class})
class EsotericDuplicatorTest extends BaseCardTest {

    @Test
    @DisplayName("A sacrificed artifact can be copied at the next end step")
    void copiesAnotherSacrificedArtifactAtNextEndStep() {
        addCreatureReady(player1, new EsotericDuplicator());
        Permanent star = addCreatureReady(player1, new ChromaticStar());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, star.getId());
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.ColorChoice) {
            harness.handleListChoice(player1, "RED");
            resolveAllTriggers();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.getDelayedActions(com.github.laxika.magicalvibes.model.action.DelayedCreateTokenCopy.class))
                .hasSize(1);

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Sacrificing Esoteric Duplicator copies the sacrificed artifact itself")
    void copiesItselfWhenSacrificed() {
        Permanent duplicator = addCreatureReady(player1, new EsotericDuplicator());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, duplicator.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
    }

    @Test
    @DisplayName("Declining the copy payment still draws a card")
    void decliningPaymentStillDrawsCard() {
        Permanent duplicator = addCreatureReady(player1, new EsotericDuplicator());
        EsotericDuplicator drawnCard = new EsotericDuplicator();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard, new EsotericDuplicator()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, duplicator.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's sacrifice does not trigger your Duplicator")
    void opponentSacrificeDoesNotTriggerYourDuplicator() {
        addCreatureReady(player1, new EsotericDuplicator());
        Permanent opponentDuplicator = addCreatureReady(player2, new EsotericDuplicator());
        harness.setLibrary(player2, List.of(new EsotericDuplicator(), new EsotericDuplicator()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, opponentDuplicator.getId());
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allSatisfy(entry ->
                assertThat(entry.getControllerId()).isEqualTo(player2.getId()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
    }

    @Test
    @DisplayName("A copied Duplicator retains its abilities and can itself be copied")
    void sacrificedTokenCanBeCopiedAgain() {
        Permanent duplicator = addCreatureReady(player1, new EsotericDuplicator());
        harness.setLibrary(player1, List.of(new EsotericDuplicator(), new EsotericDuplicator(),
                new EsotericDuplicator(), new EsotericDuplicator()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, duplicator.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, token.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard().isToken()).isTrue();
                    assertThat(permanent.getId()).isNotEqualTo(token.getId());
                });
    }

    @Test
    @DisplayName("Sacrificing during an end step waits until the next turn's end step")
    void sacrificeDuringEndStepWaitsForNextEndStep() {
        Permanent duplicator = addCreatureReady(player1, new EsotericDuplicator());
        harness.setLibrary(player1, List.of(new EsotericDuplicator(), new EsotericDuplicator()));
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, duplicator.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);

        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
    }
}