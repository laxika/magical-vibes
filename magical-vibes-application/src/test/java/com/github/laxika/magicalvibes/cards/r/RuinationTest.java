package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavageLands;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ruination.class, Forest.class, SavageLands.class, GrizzlyBears.class})
class RuinationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all nonbasic lands controlled by both players")
    void destroysAllNonbasicLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new SavageLands());
        harness.addToBattlefield(player2, new SavageLands());
        castRuinationAndResolve();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Savage Lands");
        harness.assertInGraveyard(player2, "Savage Lands");
    }

    @Test
    @DisplayName("Does not destroy non-land permanents")
    void doesNotDestroyNonLands() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castRuinationAndResolve();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Indestructible nonbasic lands survive")
    void indestructibleNonbasicLandsSurvive() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SavageLands());
        land.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castRuinationAndResolve();

        harness.assertOnBattlefield(player2, "Savage Lands");
        harness.assertNotInGraveyard(player2, "Savage Lands");
    }

    @Test
    @DisplayName("Resolves when there are no nonbasic lands")
    void resolvesWithoutNonbasicLands() {
        castRuinationAndResolve();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ruination");
    }

    private void castRuinationAndResolve() {
        harness.castFromHand(player1, new Ruination(), "{3}{R}");
        harness.passBothPriorities();
    }
}
