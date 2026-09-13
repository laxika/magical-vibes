package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelvalaExplorerReturned.class, Forest.class, GrizzlyBears.class})
class SelvalaExplorerReturnedTest extends BaseCardTest {

    private static final int STARTING_LIFE = GameData.STARTING_LIFE_TOTAL;

    @Test
    @DisplayName("Parley adds green mana and life for each revealed nonland, then draws")
    void parleyRewardsEachNonlandAndDraws() {
        Card topNonland = new GrizzlyBears();
        Card topLand = new Forest();
        Card nextPlayer1Card = new Forest();
        Card nextPlayer2Card = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topNonland, nextPlayer1Card));
        harness.setLibrary(player2, List.of(topLand, nextPlayer2Card));
        Permanent selvala = addReadySelvala();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(selvala.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(STARTING_LIFE + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(topNonland);
        assertThat(gd.playerHands.get(player2.getId())).contains(topLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextPlayer1Card);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextPlayer2Card);
    }

    @Test
    @DisplayName("Parley draws revealed lands without adding mana or life")
    void parleyDrawsLandsWithoutReward() {
        Card player1Land = new Forest();
        Card player2Land = new Forest();
        harness.setLibrary(player1, List.of(player1Land));
        harness.setLibrary(player2, List.of(player2Land));
        addReadySelvala();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(STARTING_LIFE);
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Land);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Land);
    }

    private Permanent addReadySelvala() {
        Permanent selvala = harness.addToBattlefieldAndReturn(player1, new SelvalaExplorerReturned());
        selvala.setSummoningSick(false);
        return selvala;
    }
}
