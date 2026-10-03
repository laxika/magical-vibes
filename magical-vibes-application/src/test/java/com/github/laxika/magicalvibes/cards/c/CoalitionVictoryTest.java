package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlazingSpecter;
import com.github.laxika.magicalvibes.cards.f.FiresOfYavimaya;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GalinasKnight;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoalitionVictory.class, BlazingSpecter.class, FiresOfYavimaya.class, Forest.class,
        GalinasKnight.class, Island.class, Mountain.class, NomadicElf.class, Plains.class, Swamp.class})
class CoalitionVictoryTest extends BaseCardTest {

    @Test
    @DisplayName("Wins with a land of each basic type and a creature of each color")
    void winsWithAllRequiredLandsAndColors() {
        addAllBasicLandTypes(player1);
        addAllRequiredCreatures(player1);
        castCoalitionVictory();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not win when one required basic land type is missing")
    void doesNotWinWithoutEachBasicLandType() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        addAllRequiredCreatures(player1);
        castCoalitionVictory();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not win when one required creature color is missing")
    void doesNotWinWithoutEachCreatureColor() {
        addAllBasicLandTypes(player1);
        harness.addToBattlefield(player1, new GalinasKnight());
        harness.addToBattlefield(player1, new BlazingSpecter());
        castCoalitionVictory();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("A colored noncreature does not satisfy the creature requirement")
    void coloredNoncreaturesDoNotCountAsCreatures() {
        addAllBasicLandTypes(player1);
        harness.addToBattlefield(player1, new GalinasKnight());
        harness.addToBattlefield(player1, new BlazingSpecter());
        harness.addToBattlefield(player1, new FiresOfYavimaya());
        castCoalitionVictory();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Opponent-controlled lands and creatures do not satisfy the condition")
    void opponentPermanentsDoNotCount() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        addAllRequiredCreatures(player2);
        castCoalitionVictory();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The requirements cannot be combined across players")
    void requirementsMustBeMetByTheSamePlayer() {
        addAllBasicLandTypes(player1);
        harness.addToBattlefield(player1, new GalinasKnight());
        addAllRequiredCreatures(player2);
        castCoalitionVictory();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Wins when the missing creature color is gained before resolution")
    void requirementsAreCheckedAtResolution() {
        addAllBasicLandTypes(player1);
        harness.addToBattlefield(player1, new GalinasKnight());
        harness.addToBattlefield(player1, new BlazingSpecter());
        harness.castFromHand(player1, new CoalitionVictory(), "{3}{W}{U}{B}{R}{G}");

        harness.addToBattlefield(player1, new NomadicElf());
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not win if the required land leaves before resolution")
    void losingRequiredLandBeforeResolutionPreventsWin() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        var forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        addAllRequiredCreatures(player1);
        harness.castFromHand(player1, new CoalitionVictory(), "{3}{W}{U}{B}{R}{G}");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, forest));
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertInGraveyard(player1, "Coalition Victory");
    }

    private void castCoalitionVictory() {
        harness.castFromHand(player1, new CoalitionVictory(), "{3}{W}{U}{B}{R}{G}");
        harness.passBothPriorities();
    }

    private void addAllBasicLandTypes(Player player) {
        harness.addToBattlefield(player, new Plains());
        harness.addToBattlefield(player, new Island());
        harness.addToBattlefield(player, new Swamp());
        harness.addToBattlefield(player, new Mountain());
        harness.addToBattlefield(player, new Forest());
    }

    private void addAllRequiredCreatures(Player player) {
        harness.addToBattlefield(player, new GalinasKnight());
        harness.addToBattlefield(player, new BlazingSpecter());
        harness.addToBattlefield(player, new NomadicElf());
    }
}
