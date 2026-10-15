package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.f.FieryImpulse;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.m.MacabreWaltz;
import com.github.laxika.magicalvibes.cards.m.MantleOfWebs;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NissasPilgrimage.class, Forest.class, Plains.class, Island.class,
        TimberpackWolf.class, FieryImpulse.class, MacabreWaltz.class, MantleOfWebs.class})
class NissasPilgrimageTest extends BaseCardTest {

    @Test
    @DisplayName("Only basic Forest cards are offered for the battlefield pick")
    void onlyBasicForestsOffered() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().cards()).allMatch(c -> c.getSubtypes().contains(CardSubtype.FOREST));
    }

    @Test
    @DisplayName("Without spell mastery, one Forest enters tapped and one goes to hand")
    void withoutSpellMasteryTwoCards() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.FOREST) && p.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getSubtypes().contains(CardSubtype.FOREST));
    }

    @Test
    @DisplayName("Spell mastery searches for a third Forest, putting two into hand")
    void spellMasteryThreeCards() {
        setupAndCast();
        setupLibrary();
        harness.setGraveyard(player1, List.of(new FieryImpulse(), new FieryImpulse()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.HAND);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerHands.get(player1.getId()))
                .allMatch(c -> c.getSubtypes().contains(CardSubtype.FOREST));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
    }

    @Test
    @DisplayName("Declining the battlefield pick finds nothing at all")
    void decliningBattlefieldPickFindsNothing() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("A library with only non-Forest basics prompts no search")
    void noForestsNoPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Plains(), new Island(), new TimberpackWolf()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("One prior sorcery does not count the resolving Pilgrimage for spell mastery")
    void resolvingSpellDoesNotCountItself() {
        setupAndCast();
        setupLibrary();
        harness.setGraveyard(player1, List.of(new MacabreWaltz()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Nissa's Pilgrimage");
    }

    @Test
    @DisplayName("An instant and a sorcery added after casting enable spell mastery")
    void mixedSpellTypesCountAtResolution() {
        setupAndCast();
        setupLibrary();
        harness.setGraveyard(player1, List.of(new FieryImpulse(), new MacabreWaltz()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Nonspell cards and opposing graveyards do not enable spell mastery")
    void onlyControllersInstantsAndSorceriesCount() {
        setupAndCast();
        setupLibrary();
        harness.setGraveyard(player1,
                List.of(new FieryImpulse(), new TimberpackWolf(), new Forest(), new MantleOfWebs()));
        harness.setGraveyard(player2, List.of(new FieryImpulse(), new MacabreWaltz()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("A single available Forest enters tapped with nothing put into hand")
    void onlyOneForestAvailable() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(p -> assertThat(p.isTapped()).isTrue());
        assertThat(gd.playerDecks.get(player1.getId())).singleElement().isInstanceOf(Plains.class);
    }

    @Test
    @DisplayName("The hand search may be declined after finding one Forest")
    void mayFindOnlyOneForest() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Nissa's Pilgrimage");
    }

    @Test
    @DisplayName("Spell mastery still allows stopping after finding two Forests")
    void spellMasteryMayFindOnlyTwoForests() {
        setupAndCast();
        setupLibrary();
        harness.setGraveyard(player1, List.of(new FieryImpulse(), new MacabreWaltz()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Nissa's Pilgrimage");
    }

    @Test
    @DisplayName("An empty library completes resolution without prompting")
    void emptyLibraryCompletesResolution() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Nissa's Pilgrimage");
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new NissasPilgrimage()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1,
                List.of(new Forest(), new Forest(), new Forest(), new Plains(), new TimberpackWolf()));
    }
}
