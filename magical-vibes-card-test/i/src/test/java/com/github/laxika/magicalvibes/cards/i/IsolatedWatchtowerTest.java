package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IsolatedWatchtower.class, Forest.class, GrizzlyBears.class})
class IsolatedWatchtowerTest extends BaseCardTest {

    @Test
    @DisplayName("Requires an opponent to control at least two more lands")
    void requiresOpponentToBeAtLeastTwoLandsAhead() {
        addWatchtower();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least two more lands");
    }

    @Test
    @DisplayName("Scries, then reveals and puts a basic land onto the battlefield tapped")
    void scriesThenPutsRevealedBasicLandTapped() {
        Permanent watchtower = addWatchtower();
        addOpponentLands(3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card nextCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), nextCard));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
        assertThat(watchtower.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    @DisplayName("A revealed non-basic card remains on top")
    void revealedNonBasicCardRemainsOnTop() {
        addWatchtower();
        addOpponentLands(3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private Permanent addWatchtower() {
        return harness.addToBattlefieldAndReturn(player1, new IsolatedWatchtower());
    }

    private void addOpponentLands(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player2, new Forest());
        }
    }
}
