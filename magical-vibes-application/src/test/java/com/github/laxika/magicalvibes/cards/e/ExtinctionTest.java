package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExtinctionTest extends BaseCardTest {

    private void payAndCast(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.setHand(player, List.of(new Extinction()));
        harness.castSorcery(player, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Destroys every creature of the chosen type, on any battlefield")
    void destroysAllCreaturesOfChosenType() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        payAndCast(player1);
        harness.handleListChoice(player1, "BEAR");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creatures of other types survive")
    void otherTypesSurvive() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());

        payAndCast(player1);
        harness.handleListChoice(player1, "BEAR");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A Changeling is of every creature type and is destroyed")
    void changelingIsDestroyed() {
        harness.addToBattlefield(player2, new AvianChangeling());

        payAndCast(player1);
        harness.handleListChoice(player1, "GOBLIN");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing a type nobody controls destroys nothing")
    void chosenTypeNobodyControlsDestroysNothing() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        payAndCast(player1);
        harness.handleListChoice(player1, "GOBLIN");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }
}
