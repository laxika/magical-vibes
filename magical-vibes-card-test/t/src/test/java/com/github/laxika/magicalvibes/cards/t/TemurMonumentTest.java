package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemurMonument.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class})
class TemurMonumentTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield ability searches for a Forest, Island, or Mountain")
    void searchesForATemurBasicLand() {
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new Forest(), new Island(), new Mountain(), new Plains(), new Swamp(), new TemurMonument())));
        harness.castFromHand(player1, new TemurMonument(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Island", "Mountain");

        String chosenName = search.params().cards().getFirst().getName();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, chosenName);
    }

    @Test
    @DisplayName("Sacrificing the monument creates a 5/5 green Elephant")
    void sacrificeCreatesElephant() {
        harness.addToBattlefield(player1, new TemurMonument());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Temur Monument");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elephant"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(5);
                    assertThat(token.getEffectiveToughness()).isEqualTo(5);
                    assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELEPHANT);
                });
    }

    @Test
    @DisplayName("The token ability can be activated only at sorcery speed")
    void onlyAtSorcerySpeed() {
        harness.addToBattlefield(player1, new TemurMonument());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A restricted search may find no card even when a matching land exists")
    void mayFailToFind() {
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Island(), new Mountain())));
        harness.castFromHand(player1, new TemurMonument(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The search completes without taking a Plains or Swamp when no eligible land exists")
    void noEligibleLand() {
        harness.setLibrary(player1, new ArrayList<>(List.of(new Plains(), new Swamp(), new TemurMonument())));
        harness.castFromHand(player1, new TemurMonument(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The monument is sacrificed as a cost before its token ability resolves")
    void sacrificesImmediatelyButCreatesTokenOnResolution() {
        prepareTokenAbility();

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Temur Monument");
        harness.assertNotOnBattlefield(player1, "Temur Monument");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elephant");
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(token -> {
            assertThat(token.getCard().getColors()).containsExactly(CardColor.GREEN);
            assertThat(token.getCard().getKeywords()).isEmpty();
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tapped monument cannot pay the tap cost")
    void tappedMonumentCannotActivate() {
        prepareTokenAbility();
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Temur Monument");
        harness.assertNotInGraveyard(player1, "Temur Monument");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colorless mana cannot replace the required red mana")
    void requiresRedMana() {
        harness.addToBattlefield(player1, new TemurMonument());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Temur Monument");
        harness.assertNotInGraveyard(player1, "Temur Monument");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated during its controller's upkeep")
    void cannotActivateOutsideMainPhase() {
        prepareTokenAbility();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Temur Monument");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated while a spell is on the stack")
    void cannotActivateWithNonemptyStack() {
        prepareTokenAbility();
        harness.castFromHand(player1, new TemurMonument(), "{2}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Temur Monument");
        harness.assertNotInGraveyard(player1, "Temur Monument");
        assertThat(gd.stack).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("Each eligible basic land can be revealed and put into hand")
    void canChooseEachEligibleLand(int choice) {
        List<Card> lands = List.of(new Forest(), new Island(), new Mountain());
        Card chosenLand = lands.get(choice);
        harness.setLibrary(player1, new ArrayList<>(lands));
        harness.castFromHand(player1, new TemurMonument(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, choice);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenLand);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).doesNotContain(chosenLand);
        harness.assertNotOnBattlefield(player1, chosenLand.getName());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("An empty library does not prevent the enter ability from completing")
    void emptyLibrary() {
        harness.setLibrary(player1, new ArrayList<>());
        harness.castFromHand(player1, new TemurMonument(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Temur Monument");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void prepareTokenAbility() {
        harness.addToBattlefield(player1, new TemurMonument());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
