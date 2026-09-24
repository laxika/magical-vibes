package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChromaticSphere.class, AlphaMyr.class})
class ChromaticSphereTest extends BaseCardTest {

    @Test
    @DisplayName("Activating adds mana, draws a card, and sacrifices itself")
    void activateAddsManaDrawsAndSacrifices() {
        harness.addToBattlefield(player1, new ChromaticSphere());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLibrary(player1, List.of(new AlphaMyr()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Chromatic Sphere");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without paying the generic mana cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new ChromaticSphere());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Chromatic Sphere");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenAlreadyTapped() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new ChromaticSphere());
        sphere.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Chromatic Sphere");
    }

    @Test
    @DisplayName("Can be activated during an opponent's turn")
    void canBeActivatedDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new ChromaticSphere());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new AlphaMyr()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Chromatic Sphere");
    }
}
