package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
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
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard))
                .contains(forest);
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

    private void addAbilityMana() {
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 3);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
    }
}
