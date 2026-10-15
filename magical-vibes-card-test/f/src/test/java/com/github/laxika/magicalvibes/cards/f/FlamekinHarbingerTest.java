package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.b.BoggartBirthRite;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mulldrifter;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlamekinHarbinger.class, Plains.class, Mulldrifter.class, Island.class,
        BoggartBirthRite.class, NamelessInversion.class})
class FlamekinHarbingerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Flamekin Harbinger creates a may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Flamekin Harbinger");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability only offers Elemental cards")
    void acceptingMayOffersOnlyElementals() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getSubtypes().contains(CardSubtype.ELEMENTAL));
    }

    @Test
    @DisplayName("Choosing an Elemental puts it on top of the library")
    void choosingElementalPutsOnTop() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst().getName()).isEqualTo(chosenName);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("A noncreature card with changeling can be revealed and put on top")
    void canFindNoncreatureChangeling() {
        setupAndCast();
        NamelessInversion elemental = new NamelessInversion();
        Plains plains = new Plains();
        Island island = new Island();
        harness.setLibrary(player1, List.of(plains, elemental, island));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(elemental);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(elemental);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, elemental, island);
        assertThat(gd.gameLog).anySatisfy(entry ->
                assertThat(entry.plainText()).contains("reveals Nameless Inversion"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A restricted search may fail to find even when an Elemental is present")
    void mayFailToFindWithElementalPresent() {
        setupAndCast();
        Mulldrifter elemental = new Mulldrifter();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(elemental, plains));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(elemental, plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog).anySatisfy(entry ->
                assertThat(entry.plainText()).contains("chooses not to take a card. Library is shuffled."));
    }

    @Test
    @DisplayName("Accepting a search with no matching cards completes without a choice")
    void noMatchingCardsCompletesSearch() {
        setupAndCast();
        Plains plains = new Plains();
        Island island = new Island();
        harness.setLibrary(player1, List.of(plains, island));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, island);
        assertThat(gd.gameLog).anySatisfy(entry ->
                assertThat(entry.plainText()).contains("finds no", "Library is shuffled."));
    }

    @Test
    @DisplayName("Accepting a search of an empty library completes safely")
    void emptyLibraryCompletesSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new FlamekinHarbinger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1,
                List.of(new Plains(), new Mulldrifter(), new Island(), new BoggartBirthRite()));
    }
}
