package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ScionSummoner;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeceiverOfForm.class, Forest.class, GrizzlyBears.class, HillGiant.class, ScionSummoner.class})
class DeceiverOfFormTest extends BaseCardTest {

    @Test
    @DisplayName("May have other creatures you control copy the revealed creature")
    void copiesOtherControlledCreatures() {
        Permanent deceiver = addDeceiver();
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownGiant = addCreatureReady(player1, new HillGiant());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new HillGiant(), new Forest()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownGiant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownGiant)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(8);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Declining the copy leaves creatures unchanged")
    void decliningCopyLeavesCreaturesUnchanged() {
        addDeceiver();
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new HillGiant(), new Forest()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("A noncreature reveal skips the copy choice and can still be put on the bottom")
    void noncreatureRevealSkipsCopy() {
        addDeceiver();
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new HillGiant()));

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        PendingInteraction.MayAbilityChoice choice = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.description()).contains("bottom of your library");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(HillGiant.class);
    }

    @Test
    @DisplayName("Temporary copies end at cleanup")
    void copiesEndAtCleanup() {
        addDeceiver();
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new HillGiant(), new Forest()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bottoming the revealed creature does not end the copy effect")
    void copiesPersistAfterRevealedCardIsBottomed() {
        addDeceiver();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        HillGiant revealed = new HillGiant();
        Forest nextCard = new Forest();
        harness.setLibrary(player1, List.of(revealed, nextCard));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, revealed);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Copies retain counters, noncopy bonuses, and tapped status")
    void copiesPreserveExistingModifiersAndStatus() {
        addDeceiver();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bears.setPowerModifier(1);
        bears.setToughnessModifier(2);
        bears.tap();
        harness.setLibrary(player1, List.of(new HillGiant(), new Forest()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not become copies")
    void laterCreaturesDoNotBecomeCopies() {
        addDeceiver();
        Permanent existingBears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new HillGiant(), new Forest()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        Permanent laterBears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, existingBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, laterBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The triggered ability still copies creatures after Deceiver leaves")
    void abilityResolvesWithoutSource() {
        Permanent deceiver = addDeceiver();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new HillGiant(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(deceiver);
        gd.playerGraveyards.get(player1.getId()).add(deceiver.getCard());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Deceiver does not trigger during the opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        addDeceiver();
        harness.setLibrary(player1, List.of(new HillGiant(), new Forest()));

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("reveals")).isFalse();
    }

    @Test
    @DisplayName("An empty library leaves other creatures unchanged")
    void emptyLibraryDoesNotCopyOrMoveCards() {
        addDeceiver();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of());

        advanceToCombat(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        gs.declareAttackers(gd, player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Becoming a copy does not trigger the copied enters-the-battlefield ability")
    void copyingDoesNotTriggerEnterAbilities() {
        addDeceiver();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new ScionSummoner(), new Forest()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(bears.getCard().getName()).isEqualTo("Scion Summoner");
    }

    private Permanent addDeceiver() {
        return addCreatureReady(player1, new DeceiverOfForm());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }
}
