package com.github.laxika.magicalvibes.cards.k;

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

@CardUsed({KindredDominance.class, AvianChangeling.class, BayouDragonfly.class, BenthicBehemoth.class})
class KindredDominanceTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures not of the chosen type on every battlefield")
    void destroysCreaturesNotOfChosenType() {
        harness.addToBattlefield(player1, new BenthicBehemoth());
        harness.addToBattlefield(player2, new BenthicBehemoth());
        harness.addToBattlefield(player2, new BayouDragonfly());

        castAndChoose(player1, "SERPENT");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .hasSize(1)
                .allMatch(permanent -> permanent.getCard() instanceof BenthicBehemoth);
    }

    @Test
    @DisplayName("A Changeling survives because it has the chosen type")
    void changelingSurvives() {
        harness.addToBattlefield(player2, new AvianChangeling());
        harness.addToBattlefield(player2, new BayouDragonfly());

        castAndChoose(player1, "GOBLIN");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .hasSize(1)
                .allMatch(permanent -> permanent.getCard() instanceof AvianChangeling);
    }

    private void castAndChoose(Player player, String creatureType) {
        harness.castFromHand(player, new KindredDominance(), "{5}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player, creatureType);
    }
}
