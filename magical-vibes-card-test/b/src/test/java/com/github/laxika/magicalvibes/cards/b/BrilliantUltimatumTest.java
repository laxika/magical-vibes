package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.ResoundingThunder;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrilliantUltimatum.class, BoneSplinters.class, Forest.class, GrizzlyBears.class, Island.class,
        LlanowarElves.class, Plains.class, ResoundingThunder.class, Swamp.class})
class BrilliantUltimatumTest extends BaseCardTest {

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 3);
    }

    @Test
    @DisplayName("Resolving exiles the top five cards and prompts the opponent to separate them")
    void resolutionExilesTopFiveAndPromptsOpponent() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card swamp = new Swamp();
        Card island = new Island();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(forest, bears, swamp, island, plains));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        // All five cards are exiled from the library
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(forest.getId())).isNotNull();
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();

        // Opponent (player2) is prompted to separate them into piles
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isTrue();
        PendingInteraction.BrilliantUltimatumPileSeparationChoice choice =
                gd.interaction.activeInteraction(
                        PendingInteraction.BrilliantUltimatumPileSeparationChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).hasSize(5);
    }

    @Test
    @DisplayName("Chosen pile: the controller plays a land and casts a creature for free; the other pile stays exiled")
    void controllerPlaysLandAndCastsSpellFromChosenPile() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card swamp = new Swamp();
        Card island = new Island();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(forest, bears, swamp, island, plains));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        // Opponent puts forest + bears in Pile 1
        harness.handleMultipleCardsChosen(player2, List.of(forest.getId(), bears.getId()));

        // Controller chooses Pile 1
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.BrilliantUltimatumPileChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Controller is offered the pile's cards to play
        PendingInteraction.BrilliantUltimatumPlayChoice playChoice =
                gd.interaction.activeInteraction(PendingInteraction.BrilliantUltimatumPlayChoice.class);
        assertThat(playChoice).isNotNull();
        assertThat(playChoice.validCardIds()).containsExactlyInAnyOrder(forest.getId(), bears.getId());

        // Controller plays both
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), bears.getId()));

        // The land is on the battlefield and used the land play
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(forest.getId()));
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(1);

        // The creature was cast for free — it is on the stack; resolve it
        assertThat(gd.stack).anyMatch(e -> e.getCard().getId().equals(bears.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(bears.getId()));

        // The unchosen pile (swamp, island, plains) remains exiled
        assertThat(gd.findExiledCard(swamp.getId())).isNotNull();
        assertThat(gd.findExiledCard(island.getId())).isNotNull();
        assertThat(gd.findExiledCard(plains.getId())).isNotNull();

        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("Declining the pile choice plays from the other pile")
    void controllerChoosesPileTwo() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card swamp = new Swamp();
        Card island = new Island();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(bears, elves, swamp, island, plains));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        // Opponent puts bears in Pile 1, everything else in Pile 2
        harness.handleMultipleCardsChosen(player2, List.of(bears.getId()));

        // Controller chooses Pile 2 (elves + the lands)
        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.BrilliantUltimatumPlayChoice playChoice =
                gd.interaction.activeInteraction(PendingInteraction.BrilliantUltimatumPlayChoice.class);
        assertThat(playChoice.validCardIds())
                .containsExactlyInAnyOrder(elves.getId(), swamp.getId(), island.getId(), plains.getId());

        // Cast elves only from the chosen pile
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(elves.getId()));
        // Bears was in the pile the controller did not choose — it stays exiled
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
    }

    @Test
    @DisplayName("A land can't be played if one was already played this turn; it stays exiled")
    void secondLandCannotBePlayed() {
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card bears = new GrizzlyBears();
        Card island = new Island();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(forest, swamp, bears, island, plains));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();
        // Controller has already played their land for the turn
        gd.landsPlayedThisTurn.put(player1.getId(), 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleMultipleCardsChosen(player2, List.of(forest.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        // The land could not be played (limit reached) — it stays exiled, not on the battlefield
        assertThat(gd.findExiledCard(forest.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(forest.getId()));
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    void fewerThanFiveCardsAreAllExiled() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(forest.getId())).isNotNull();
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.BrilliantUltimatumPileSeparationChoice.class).validCardIds())
                .containsExactlyInAnyOrder(forest.getId(), bears.getId());
    }

    @Test
    void emptyLibraryFinishesWithoutAChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof BrilliantUltimatum);
    }

    @Test
    void choosingAnEmptyPileLeavesAllCardsExiled() {
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleMultipleCardsChosen(player2, List.of());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerMayDeclineToPlayEveryCard() {
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player2, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleSelectedLandsStillUseOnlyOneLandPlay() {
        Card forest = new Forest();
        Card swamp = new Swamp();
        harness.setLibrary(player1, List.of(forest, swamp));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player2, List.of(forest.getId(), swamp.getId()));
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), swamp.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(forest.getId()))
                .noneMatch(p -> p.getCard().getId().equals(swamp.getId()));
        assertThat(gd.findExiledCard(swamp.getId())).isNotNull();
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    void spellsAreStackedInTheControllersChosenOrder() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(bears, elves));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player2, List.of(bears.getId(), elves.getId()));
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMultipleCardsChosen(player1, List.of(elves.getId(), bears.getId()));

        assertThat(gd.stack).extracting(entry -> entry.getCard().getId())
                .containsExactly(elves.getId(), bears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotPlayACardFromTheUnchosenPile() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player2, List.of(forest.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(bears.getId())))
                .isInstanceOf(RuntimeException.class);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(bears.getId()));
    }

    @Test
    void opponentCannotAnswerTheControllersPlayChoice() {
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player2, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(bears.getId())))
                .isInstanceOf(RuntimeException.class);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
    }

    @Test
    @CardUsed({ResoundingThunder.class})
    void spellCanBeCastBeforeTheSelectedLandIsPlayed() {
        Card thunder = new ResoundingThunder();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(thunder, forest));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player2, List.of(thunder.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMultipleCardsChosen(player1, List.of(thunder.getId(), forest.getId()));

        // Choosing the first spell's target precedes playing the next selected card.
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.findExiledCard(forest.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(forest.getId()));
    }

    @Test
    @CardUsed({BoneSplinters.class})
    void payableAdditionalCostsAreOfferedRatherThanSkippingTheSpell() {
        Card splinters = new BoneSplinters();
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(splinters));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player2, List.of(splinters.getId()));
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMultipleCardsChosen(player1, List.of(splinters.getId()));

        // The sacrifice cost is payable and a legal target exists, so casting must prompt
        // for the cost or target instead of silently leaving the selected spell in exile.
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}
