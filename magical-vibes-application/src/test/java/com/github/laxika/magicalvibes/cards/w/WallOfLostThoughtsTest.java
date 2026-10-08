package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfLostThoughts.class, Forest.class, Island.class, Mountain.class, Plains.class})
class WallOfLostThoughtsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills four cards from target player's library")
    void etbMillsFourCardsFromTargetPlayersLibrary() {
        harness.setLibrary(player2, List.of(new Forest(), new Island(), new Mountain(), new Plains()));
        harness.setHand(player1, List.of(new WallOfLostThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("ETB can target its controller")
    void etbCanTargetItsController() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain(), new Plains()));
        harness.setHand(player1, List.of(new WallOfLostThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("ETB mills only the top four cards when the library is larger")
    void etbMillsOnlyTopFourCards() {
        var forest = new Forest();
        var island = new Island();
        var mountain = new Mountain();
        var plains = new Plains();
        var remaining = new Island();
        harness.setLibrary(player2, List.of(forest, island, mountain, plains, remaining));
        harness.setHand(player1, List.of(new WallOfLostThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(forest, island, mountain, plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB mills all available cards from a short library without causing a loss")
    void etbMillsShortLibrary() {
        var forest = new Forest();
        var island = new Island();
        harness.setLibrary(player2, List.of(forest, island));
        harness.setHand(player1, List.of(new WallOfLostThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();
        harness.runStateBasedActions();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(forest, island);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("ETB can target an empty library without causing a loss")
    void etbCanTargetEmptyLibrary() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new WallOfLostThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Wall of Lost Thoughts");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Defender prevents attacking even after summoning sickness ends")
    void cannotAttackWithDefender() {
        var wall = addCreatureReady(player1, new WallOfLostThoughts());

        assertThat(als.canAttack(gd, wall, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("The creature spell has no target; the mill target is chosen after entry")
    void choosesMillTargetOnlyAfterEntering() {
        var forest = new Forest();
        harness.setLibrary(player2, List.of(forest));
        harness.setHand(player1, List.of(new WallOfLostThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wall of Lost Thoughts");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("A target supplied while casting does not replace the trigger-time choice")
    void stillChoosesTriggerTargetAfterCastTimeTargetWasSupplied() {
        var forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new WallOfLostThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wall of Lost Thoughts");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
    }
}
