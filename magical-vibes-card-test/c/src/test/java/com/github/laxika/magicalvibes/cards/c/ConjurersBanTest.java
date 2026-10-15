package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.q.Quicken;
import com.github.laxika.magicalvibes.cards.s.SkarrgTheRagePits;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConjurersBan.class, Quicken.class, SkarrgTheRagePits.class})
class ConjurersBanTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a name draws a card and prevents that spell for every player")
    void preventsChosenSpellForEveryPlayer() {
        harness.setHand(player2, List.of(new Quicken()));
        harness.setLibrary(player1, List.of(new SkarrgTheRagePits()));

        harness.castFromHand(player1, new ConjurersBan(), "{W}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Quicken");

        harness.assertInHand(player1, "Skarrg, the Rage Pits");

        harness.setHand(player1, List.of(new Quicken()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new Quicken()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Choosing a land name prevents that land from being played for every player")
    void preventsChosenLandForEveryPlayer() {
        harness.setHand(player2, List.of(new SkarrgTheRagePits()));
        harness.setLibrary(player1, List.of(new Quicken()));

        harness.castFromHand(player1, new ConjurersBan(), "{W}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Skarrg, the Rage Pits");

        harness.setHand(player1, List.of(new SkarrgTheRagePits()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new SkarrgTheRagePits()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.playLand(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The restriction ends at the beginning of the controller's next turn")
    void restrictionEndsAtControllersNextTurn() {
        harness.setHand(player2, List.of(new Quicken()));

        harness.castFromHand(player1, new ConjurersBan(), "{W}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Quicken");

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        harness.setHand(player1, List.of(new Quicken()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Quicken");

        harness.setHand(player1, List.of(new SkarrgTheRagePits()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);

        harness.assertOnBattlefield(player1, "Skarrg, the Rage Pits");
    }

    @Test
    @DisplayName("Exactly one card is drawn after the name is chosen")
    void drawsOneCardAfterChoosingName() {
        harness.setLibrary(player1, List.of(new SkarrgTheRagePits(), new Quicken()));
        harness.castFromHand(player1, new ConjurersBan(), "{W}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleListChoice(player1, "Quicken");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Skarrg, the Rage Pits");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Conjurer's Ban");
    }

    @Test
    @DisplayName("Naming a land does not prevent activating its mana ability")
    void namedLandCanStillTapForMana() {
        harness.addToBattlefield(player1, new SkarrgTheRagePits());
        harness.setLibrary(player1, List.of(new Quicken()));
        harness.castFromHand(player1, new ConjurersBan(), "{W}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Skarrg, the Rage Pits");

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(findPermanent(player1, "Skarrg, the Rage Pits").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("A banned land becomes playable when the controller's next turn begins")
    void chosenLandRestrictionExpires() {
        harness.setLibrary(player1, List.of(new Quicken(), new Quicken()));
        harness.setHand(player2, List.of(new SkarrgTheRagePits()));
        harness.castFromHand(player1, new ConjurersBan(), "{W}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Skarrg, the Rage Pits");

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new SkarrgTheRagePits()));
        harness.playLand(player1, 0);

        harness.assertOnBattlefield(player1, "Skarrg, the Rage Pits");
    }

    @Test
    @DisplayName("Naming a land does not prevent casting a differently named spell")
    void otherNamesRemainPlayable() {
        harness.setLibrary(player1, List.of(new SkarrgTheRagePits(), new SkarrgTheRagePits()));
        harness.castFromHand(player1, new ConjurersBan(), "{W}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Skarrg, the Rage Pits");

        harness.castFromHand(player1, new Quicken(), "{U}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Quicken");
    }

    @Test
    @DisplayName("A real card name may be chosen even when no copy is in the game")
    void canChooseAbsentCardName() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new SkarrgTheRagePits()));
        harness.setLibrary(player2, List.of(new SkarrgTheRagePits()));
        harness.castFromHand(player1, new ConjurersBan(), "{W}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Quicken");

        harness.assertInHand(player1, "Skarrg, the Rage Pits");
        assertThatThrownBy(() -> harness.castFromHand(player1, new Quicken(), "{U}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The chosen name must belong to a real Oracle card")
    void rejectsNonexistentCardName() {
        harness.setLibrary(player1, List.of(new SkarrgTheRagePits()));
        harness.castFromHand(player1, new ConjurersBan(), "{W}{B}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "This Is Not An Oracle Card Name"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing a name does not reveal cards in the opponent's hidden zones")
    void namePromptDoesNotRevealOpponentsHiddenCards() {
        harness.setHand(player2, List.of(new SkarrgTheRagePits()));
        harness.setLibrary(player2, List.of(new Quicken()));
        harness.setLibrary(player1, List.of(new ConjurersBan()));
        harness.clearMessages();
        harness.castFromHand(player1, new ConjurersBan(), "{W}{B}");
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("Choose a card name."))
                .isNotEmpty()
                .allSatisfy(message -> assertThat(message)
                        .doesNotContain("Skarrg, the Rage Pits", "Quicken"));
    }
}
