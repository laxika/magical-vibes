package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuidingSpirit.class, Forest.class, GrizzlyBears.class})
class GuidingSpiritTest extends BaseCardTest {

    private Permanent spirit;
    private int spiritIndex;

    @BeforeEach
    void setUpSpirit() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        spirit = addCreatureReady(player1, new GuidingSpirit());
        spiritIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spirit);
    }

    @Test
    @DisplayName("Puts the top creature card of the targeted graveyard on top of that player's library")
    void putsTopCreatureOntoLibrary() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(forest, bears));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, spiritIndex, null, player1.getId());
        harness.passBothPriorities();

        assertThat(spirit.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        List<Card> deck = List.copyOf(gd.playerDecks.get(player1.getId()));
        assertThat(deck).hasSize(2);
        assertThat(deck.getFirst()).isSameAs(bears);
    }

    @Test
    @DisplayName("Does nothing when the top graveyard card is not a creature")
    void nonCreatureTopIsNoOp() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears, forest));
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, spiritIndex, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears, forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does nothing when the targeted graveyard is empty")
    void emptyGraveyardIsNoOp() {
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player1, spiritIndex, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can target an opponent whose top graveyard card is a creature")
    void canTargetOpponent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, spiritIndex, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(List.copyOf(gd.playerDecks.get(player2.getId())).getFirst()).isSameAs(bears);
    }

    @Test
    @DisplayName("Cannot activate again while tapped")
    void cannotActivateWhileTapped() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, spiritIndex, null, player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, spiritIndex, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires a player target")
    void requiresPlayerTarget() {
        assertThatThrownBy(() -> harness.activateAbility(player1, spiritIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target player");
    }

    @Test
    @DisplayName("Rejects a permanent as a player target")
    void rejectsPermanentAsPlayerTarget() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, spiritIndex, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
