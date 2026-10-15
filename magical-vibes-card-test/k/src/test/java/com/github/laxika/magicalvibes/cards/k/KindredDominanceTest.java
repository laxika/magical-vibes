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

    @Test
    @DisplayName("Destroys the caster's nonmatching creatures as well as the opponent's")
    void destroysNonmatchingCreaturesOnBothBattlefields() {
        harness.addToBattlefield(player1, new BayouDragonfly());
        harness.addToBattlefield(player2, new BayouDragonfly());
        harness.addToBattlefield(player2, new BenthicBehemoth());

        castAndChoose(player1, "SERPENT");

        harness.assertNotOnBattlefield(player1, "Bayou Dragonfly");
        harness.assertNotOnBattlefield(player2, "Bayou Dragonfly");
        harness.assertInGraveyard(player1, "Bayou Dragonfly");
        harness.assertInGraveyard(player2, "Bayou Dragonfly");
        harness.assertOnBattlefield(player2, "Benthic Behemoth");
        harness.assertInGraveyard(player1, "Kindred Dominance");
    }

    @Test
    @DisplayName("Choosing a type absent from the battlefield destroys every creature")
    void canChooseAnAbsentCreatureType() {
        harness.addToBattlefield(player1, new BenthicBehemoth());
        harness.addToBattlefield(player2, new BayouDragonfly());

        castAndChoose(player1, "GOBLIN");

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Benthic Behemoth");
        harness.assertInGraveyard(player2, "Bayou Dragonfly");
    }

    @Test
    @DisplayName("Resolves on an empty battlefield after choosing a creature type")
    void resolvesOnEmptyBattlefield() {
        castAndChoose(player1, "GOBLIN");

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Kindred Dominance");
    }

    private void castAndChoose(Player player, String creatureType) {
        harness.castFromHand(player, new KindredDominance(), "{5}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player, creatureType);
    }
}
