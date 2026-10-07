package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoneDocent.class})
class StoneDocentTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Stone Docent from the graveyard gains 2 life, surveils, and exiles it")
    void activationGainsLifeSurveilsAndExilesSource() {
        Card topCard = new StoneDocent();
        harness.setGraveyard(player1, List.of(new StoneDocent()));
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        harness.assertInGraveyard(player1, "Stone Docent");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Stone Docent"));
    }

    @Test
    @DisplayName("Declining surveil leaves the top card on the library")
    void decliningSurveilLeavesTopCardOnLibrary() {
        Card topCard = new StoneDocent();
        harness.setGraveyard(player1, List.of(new StoneDocent()));
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Stone Docent's graveyard ability can only be activated at sorcery speed")
    void activationOnlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new StoneDocent()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Stone Docent");
    }

    @Test
    @DisplayName("An empty library does not prevent gaining life; exile is paid before resolution")
    void emptyLibraryStillGainsLife() {
        Card source = new StoneDocent();
        harness.setGraveyard(player1, List.of(source));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Stone Docent");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(source);
        harness.assertLife(player1, lifeBefore);

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated during its controller's upkeep")
    void cannotActivateOutsideMainPhase() {
        harness.setGraveyard(player1, List.of(new StoneDocent()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Stone Docent");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A second Stone Docent cannot be activated while the first ability is on the stack")
    void cannotActivateWithNonemptyStack() {
        Card first = new StoneDocent();
        Card second = new StoneDocent();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Without white mana the source stays in the graveyard")
    void cannotActivateWithoutWhiteMana() {
        harness.setGraveyard(player1, List.of(new StoneDocent()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int lifeBefore = gd.getLife(player1.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Stone Docent");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).isEmpty();
    }
}
