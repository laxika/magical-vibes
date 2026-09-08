package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnchartedVoyageTest extends BaseCardTest {

    @Test
    @DisplayName("The target creature's owner can put it on the bottom, then you surveil 1")
    void ownerChoosesBottomThenControllerSurveils() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card targetLibraryCard = new Island();
        Card surveilCard = new GrizzlyBears();
        setDeck(player2, List.of(targetLibraryCard));
        setDeck(player1, List.of(surveilCard));

        castUnchartedVoyage(target.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player2.getId());

        harness.handleListChoice(player2, "Bottom");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(targetLibraryCard, target.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(surveilCard);
        harness.assertInGraveyard(player1, "Uncharted Voyage");
    }

    @Test
    @DisplayName("The target creature's owner can keep it on top")
    void ownerChoosesTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card targetLibraryCard = new Island();
        setDeck(player2, List.of(targetLibraryCard));
        setDeck(player1, List.of(new Island()));

        castUnchartedVoyage(target.getId());

        harness.handleListChoice(player2, "Top");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), targetLibraryCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new UnchartedVoyage()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castUnchartedVoyage(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new UnchartedVoyage()));
        addMana();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
