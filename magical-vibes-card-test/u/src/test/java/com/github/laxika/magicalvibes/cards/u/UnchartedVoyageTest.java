package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnchartedVoyage.class, BearCub.class, Island.class})
class UnchartedVoyageTest extends BaseCardTest {

    @Test
    @DisplayName("The target creature's owner can put it on the bottom, then you surveil 1")
    void ownerChoosesBottomThenControllerSurveils() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearCub());
        Card targetLibraryCard = new Island();
        Card surveilCard = new BearCub();
        harness.setLibrary(player2, List.of(targetLibraryCard));
        harness.setLibrary(player1, List.of(surveilCard));

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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearCub());
        Card targetLibraryCard = new Island();
        harness.setLibrary(player2, List.of(targetLibraryCard));
        harness.setLibrary(player1, List.of(new Island()));

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

    @Test
    @DisplayName("Returning your own creature to the top lets you surveil that creature")
    void surveilsOwnReturnedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearCub());
        Card originalTop = new Island();
        harness.setLibrary(player1, List.of(originalTop));

        castUnchartedVoyage(target.getId());
        harness.handleListChoice(player1, "Top");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
        harness.assertInGraveyard(player1, "Uncharted Voyage");
    }

    @Test
    @DisplayName("An illegal target prevents surveil as well as the library move")
    void doesNotSurveilWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearCub());
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new UnchartedVoyage()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        harness.assertInGraveyard(player1, "Uncharted Voyage");
    }

    @Test
    @DisplayName("An empty library does not prevent returning the target creature")
    void resolvesWithEmptyControllerLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearCub());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        castUnchartedVoyage(target.getId());
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Uncharted Voyage");
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

}
