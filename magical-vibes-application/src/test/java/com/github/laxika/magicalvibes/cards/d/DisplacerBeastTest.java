package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DisplacerBeast.class)
class DisplacerBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield makes its controller venture into a dungeon")
    void entersDungeon() {
        castDisplacerBeast(player1);

        resolveFirstVenture();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Displacement returns Displacer Beast to its owner's hand")
    void returnsItselfToHand() {
        castDisplacerBeast(player1);

        resolveFirstVenture();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(DisplacerBeast.class);
    }

    private void castDisplacerBeast(Player player) {
        harness.castFromHand(player, new DisplacerBeast(), "{2}{U}");
    }

    private void resolveFirstVenture() {
        harness.setLibrary(player1, java.util.List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        harness.passBothPriorities();
    }

    @Test
    void advancesExistingDungeonAndResolvesChosenRoom() {
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        castDisplacerBeast(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Mine Tunnels");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 2));
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    void returnsToOwnerWhenAnotherPlayerControlsIt() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        DisplacerBeast beast = new DisplacerBeast();
        beast.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, beast);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(beast);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void secondActivationDoesNotReturnAnotherBeast() {
        harness.setHand(player1, java.util.List.of());
        harness.addToBattlefield(player1, new DisplacerBeast());
        harness.addToBattlefield(player1, new DisplacerBeast());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
