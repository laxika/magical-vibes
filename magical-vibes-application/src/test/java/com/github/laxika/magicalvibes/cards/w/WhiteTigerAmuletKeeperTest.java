package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhiteTigerAmuletKeeper.class, Forest.class, Island.class})
class WhiteTigerAmuletKeeperTest extends BaseCardTest {

    @Test
    void exilesItselfDrawsAndMayPutLandOntoBattlefield() {
        WhiteTigerAmuletKeeper tiger = new WhiteTigerAmuletKeeper();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setGraveyard(player1, List.of(tiger));
        harness.setHand(player1, List.of(forest));
        harness.setLibrary(player1, List.of(island));
        addAbilityMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(tiger);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        harness.assertOnBattlefield(player1, forest.getName());
    }

    @Test
    void decliningStillDrawsAndLeavesLandInHand() {
        WhiteTigerAmuletKeeper tiger = new WhiteTigerAmuletKeeper();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setGraveyard(player1, List.of(tiger));
        harness.setHand(player1, List.of(forest));
        harness.setLibrary(player1, List.of(island));
        addAbilityMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(tiger);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, island);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void canPutTheNewlyDrawnLandOntoTheBattlefield() {
        WhiteTigerAmuletKeeper tiger = new WhiteTigerAmuletKeeper();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(tiger));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));
        addAbilityMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, forest.getName()).isTapped()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(tiger);
    }

    @Test
    void acceptingWithoutALandInHandStillCompletesTheAbility() {
        WhiteTigerAmuletKeeper tiger = new WhiteTigerAmuletKeeper();
        WhiteTigerAmuletKeeper drawnCreature = new WhiteTigerAmuletKeeper();
        harness.setGraveyard(player1, List.of(tiger));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCreature));
        addAbilityMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilesAsAnActivationCostBeforeDrawing() {
        WhiteTigerAmuletKeeper tiger = new WhiteTigerAmuletKeeper();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(tiger));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));
        addAbilityMana();

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(tiger);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
    }

    private void addAbilityMana() {
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 3);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
    }
}
