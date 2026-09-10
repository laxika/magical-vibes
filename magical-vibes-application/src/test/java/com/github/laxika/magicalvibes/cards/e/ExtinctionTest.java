package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.b.BayouDragonfly;
import com.github.laxika.magicalvibes.cards.b.BenthicBehemoth;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Extinction.class, AvianChangeling.class, BayouDragonfly.class, BenthicBehemoth.class})
class ExtinctionTest extends BaseCardTest {

    private void payAndCast(Player player) {
        harness.castFromHand(player, new Extinction(), "{4}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Destroys every creature of the chosen type, on any battlefield")
    void destroysAllCreaturesOfChosenType() {
        harness.addToBattlefield(player1, new BenthicBehemoth());
        harness.addToBattlefield(player2, new BenthicBehemoth());
        harness.addToBattlefield(player2, new BenthicBehemoth());

        payAndCast(player1);
        harness.handleListChoice(player1, "SERPENT");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creatures of other types survive")
    void otherTypesSurvive() {
        harness.addToBattlefield(player1, new BayouDragonfly());
        harness.addToBattlefield(player2, new BenthicBehemoth());

        payAndCast(player1);
        harness.handleListChoice(player1, "SERPENT");

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
        harness.addToBattlefield(player1, new BenthicBehemoth());
        harness.addToBattlefield(player2, new BayouDragonfly());

        payAndCast(player1);
        harness.handleListChoice(player1, "GOBLIN");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }
}
