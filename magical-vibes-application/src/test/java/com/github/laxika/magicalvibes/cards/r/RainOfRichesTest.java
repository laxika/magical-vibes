package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({RainOfRiches.class, GrizzlyBears.class, LlanowarElves.class, Hurricane.class, SongOfTheDryads.class})
class RainOfRichesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two Treasures")
    void entersWithTwoTreasures() {
        castRainOfRiches();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("The first spell using Treasure mana each turn cascades")
    void firstTreasureManaSpellCascades() {
        castRainOfRiches();
        LlanowarElves cascadeHit = new LlanowarElves();
        harness.setLibrary(player1, List.of(cascadeHit));

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent treasure = findPermanents(player1, "Treasure").getFirst();
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Llanowar Elves");
    }

    @Test
    @DisplayName("A spell without Treasure mana does not cascade")
    void spellWithoutTreasureManaDoesNotCascade() {
        castRainOfRiches();
        LlanowarElves cascadeHit = new LlanowarElves();
        harness.setLibrary(player1, List.of(cascadeHit));

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("The second Treasure-funded spell each turn does not cascade")
    void secondTreasureManaSpellDoesNotCascade() {
        castRainOfRiches();
        LlanowarElves firstHit = new LlanowarElves();
        harness.setLibrary(player1, List.of(firstHit));

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addTreasureManaAndCastFirstTreasureSpell();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        LlanowarElves untouched = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).add(untouched);

        addTreasureManaAndCastSecondTreasureSpell();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(untouched);
    }

    @Test
    @DisplayName("Cascade includes the chosen X in the funded spell's mana value")
    void cascadeUsesChosenX() {
        castRainOfRiches();
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(hit));
        harness.setHand(player1, List.of(new Hurricane()));
        activateTreasureForGreen();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch choice = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice).isNotNull();
        assertThat(choice.params().cards()).containsExactly(hit);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Declining cascade returns the hit to the library and consumes the turn's grant")
    void decliningCascadeStillConsumesGrant() {
        castRainOfRiches();
        LlanowarElves hit = new LlanowarElves();
        harness.setLibrary(player1, List.of(hit));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        addTreasureManaAndCastFirstTreasureSpell();
        harness.handleCardChosen(player1, -1);
        resolveAllTriggers();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(hit);
        assertThat(gd.findExiledCard(hit.getId())).isNull();
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");

        addTreasureManaAndCastSecondTreasureSpell();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(hit);
    }

    @Test
    @DisplayName("Multiple copies grant separate cascade abilities to the same spell")
    void multipleCopiesEachGrantCascade() {
        castRainOfRiches();
        harness.addToBattlefield(player1, new RainOfRiches());
        LlanowarElves first = new LlanowarElves();
        LlanowarElves second = new LlanowarElves();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new GrizzlyBears()));

        addTreasureManaAndCastFirstTreasureSpell();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        PendingInteraction.LibrarySearch choice = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice).isNotNull();
        assertThat(choice.params().cards()).containsExactly(second);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Llanowar Elves")).hasSize(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Treasure mana spent to cast Rain of Riches consumes the turn's qualifying spell")
    void treasureFundedRainDoesNotGrantCascadeLaterThatTurn() {
        castRainOfRiches();
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Rain of Riches"));
        harness.setHand(player1, List.of(new RainOfRiches()));
        activateTreasureForGreen();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        LlanowarElves hit = new LlanowarElves();
        harness.setLibrary(player1, List.of(hit));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addTreasureManaAndCastSecondTreasureSpell();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(hit);
    }

    @Test
    @DisplayName("The grant is available again on a later turn")
    void grantResetsEachTurn() {
        castRainOfRiches();
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addTreasureManaAndCastFirstTreasureSpell();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        LlanowarElves hit = new LlanowarElves();
        harness.setLibrary(player1, List.of(hit));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addTreasureManaAndCastFirstTreasureSpell();

        PendingInteraction.LibrarySearch choice = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice).isNotNull();
        assertThat(choice.params().cards()).containsExactly(hit);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Llanowar Elves")).hasSize(2);
    }

    private void castRainOfRiches() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new RainOfRiches()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Rain of Riches turned into a Forest no longer grants cascade")
    void forestTransformationRemovesCascadeGrant() {
        castRainOfRiches();
        Permanent rain = findPermanent(player1, "Rain of Riches");
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, rain.getId());
        resolveAllTriggers();

        LlanowarElves hit = new LlanowarElves();
        harness.setLibrary(player1, List.of(hit));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addTreasureManaAndCastSecondTreasureSpell();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(hit);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void addTreasureManaAndCastFirstTreasureSpell() {
        activateTreasureForGreen();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void addTreasureManaAndCastSecondTreasureSpell() {
        activateTreasureForGreen();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

    private void activateTreasureForGreen() {
        Permanent treasure = findPermanents(player1, "Treasure").getFirst();
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "GREEN");
    }
}
