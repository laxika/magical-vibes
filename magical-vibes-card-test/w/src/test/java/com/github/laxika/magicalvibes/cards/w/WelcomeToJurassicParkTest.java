package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.cards.j.JurassicPark;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.r.RaptorHatchling;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WelcomeToJurassicPark.class, JurassicPark.class, MindStone.class, RaptorHatchling.class,
        Confiscate.class})
class WelcomeToJurassicParkTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I animates an opponent's noncreature artifact into a Wall")
    void chapterIAnimatesArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, artifact)).contains(CardSubtype.WALL);
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Chapter II creates a hasty trampling Dinosaur")
    void chapterIICreatesDinosaur() {
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent dinosaur = findPermanent(player1, "Dinosaur");
        assertThat(dinosaur).isNotNull();
        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dinosaur)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, dinosaur)).contains(CardSubtype.DINOSAUR);
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Chapter III destroys Walls before transforming into Jurassic Park")
    void chapterIIIDestroysWallsAndTransforms() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent saga = addSagaWithLore(0);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mind Stone");
        Permanent park = findPermanent(player1, "Jurassic Park");
        assertThat(park).isNotNull();
        assertThat(park.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Jurassic Park adds green mana for each Dinosaur you control")
    void jurassicParkAddsManaForDinosaurs() {
        Permanent park = harness.addToBattlefieldAndReturn(player1, new JurassicPark());
        harness.addToBattlefield(player1, new RaptorHatchling());
        harness.addToBattlefield(player1, new RaptorHatchling());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(park), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Jurassic Park gives Dinosaur cards escape")
    void jurassicParkGivesDinosaursEscape() {
        harness.addToBattlefield(player1, new JurassicPark());
        List<RaptorHatchling> graveyard = List.of(
                new RaptorHatchling(), new RaptorHatchling(), new RaptorHatchling(), new RaptorHatchling());
        harness.setGraveyard(player1, List.copyOf(graveyard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raptor Hatchling");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Chapter I excludes your artifacts and opposing creatures")
    void chapterITargetFilter() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RaptorHatchling());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(artifact.getId())
                .doesNotContain(ownArtifact.getId(), creature.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ownArtifact)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).doesNotContain(CardSubtype.WALL);
    }

    @Test
    @DisplayName("Chapter I permits choosing no artifacts")
    void chapterICanChooseNoTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
    }

    @Test
    @DisplayName("Chapter I animation ends when you lose control of the Saga")
    void animationEndsOnSagaControlChange() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Confiscate()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castEnchantment(player2, 0, saga.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(saga);
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, artifact)).doesNotContain(CardSubtype.WALL);
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Chapter II haste expires while trample remains")
    void dinosaurLosesHasteAfterTurn() {
        addSagaWithLore(1);
        advanceToNextChapter();
        harness.passBothPriorities();
        Permanent dinosaur = findPermanent(player1, "Dinosaur");

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Chapter III transforms even when there are no Walls")
    void chapterIIITransformsWithoutWalls() {
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new RaptorHatchling());
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dinosaur);
        assertThat(findPermanent(player1, "Jurassic Park").isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Jurassic Park counts only your Dinosaurs and can produce zero mana")
    void manaCountsOnlyControlledDinosaurs() {
        Permanent park = harness.addToBattlefieldAndReturn(player1, new JurassicPark());
        harness.addToBattlefield(player2, new RaptorHatchling());
        harness.addToBattlefield(player1, new MindStone());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(park), null, null);

        assertThat(park.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Jurassic Park does not grant escape to non-Dinosaur cards")
    void nonDinosaursCannotEscape() {
        harness.addToBattlefield(player1, new JurassicPark());
        harness.setGraveyard(player1, List.of(
                new MindStone(), new RaptorHatchling(), new RaptorHatchling(), new RaptorHatchling()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Mind Stone");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Escape requires three other cards and the Dinosaur's mana cost")
    void escapeRequiresFullCost() {
        harness.addToBattlefield(player1, new JurassicPark());
        harness.setGraveyard(player1, List.of(
                new RaptorHatchling(), new MindStone(), new MindStone(), new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Raptor Hatchling");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter I animates one artifact for each opponent")
    void chapterIAnimatesEachOpponentsArtifact() {
        Player opponent = new Player(UUID.randomUUID(), "Charlie");
        UUID id = opponent.getId();
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(opponent, new MindStone());
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, firstArtifact.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(secondArtifact.getId());
        harness.handlePermanentChosen(player1, secondArtifact.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        for (Permanent artifact : List.of(firstArtifact, secondArtifact)) {
            assertThat(gqs.isCreature(gd, artifact)).isTrue();
            assertThat(gqs.getEffectivePower(gd, artifact)).isZero();
            assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
            assertThat(gqs.effectiveCreatureSubtypes(gd, artifact)).contains(CardSubtype.WALL);
            assertThat(gqs.hasKeyword(gd, artifact, Keyword.DEFENDER)).isTrue();
        }
    }

    @Test
    @DisplayName("Chapter I cannot target two artifacts controlled by the same opponent")
    void chapterIRejectsTwoTargetsForOneOpponent() {
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        addSagaWithLore(0);

        advanceToNextChapter();

        harness.handlePermanentChosen(player1, firstArtifact.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, firstArtifact)).isTrue();
        assertThat(gqs.isCreature(gd, secondArtifact)).isFalse();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new WelcomeToJurassicPark());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
