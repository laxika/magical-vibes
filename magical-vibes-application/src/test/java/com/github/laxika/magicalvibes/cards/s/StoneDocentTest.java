package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoneDocentTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Stone Docent from the graveyard gains 2 life, surveils, and exiles it")
    void activationGainsLifeSurveilsAndExilesSource() {
        Card topCard = new GrizzlyBears();
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
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Stone Docent"));
    }

    @Test
    @DisplayName("Declining surveil leaves the top card on the library")
    void decliningSurveilLeavesTopCardOnLibrary() {
        Card topCard = new GrizzlyBears();
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
}
