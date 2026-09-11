package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AjaniOutlandChaperone;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmnathLocusOfCreation.class, AjaniOutlandChaperone.class, Forest.class})
class OmnathLocusOfCreationTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it enters the battlefield")
    void drawsOnEnter() {
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        harness.enterBattlefieldAndReturn(player1, new OmnathLocusOfCreation());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Landfall resolves its first three branches and then stops")
    void landfallBranchesByResolutionCount() {
        harness.addToBattlefield(player1, new OmnathLocusOfCreation());
        Permanent ownPlaneswalker = addPlaneswalker(player1, 5);
        Permanent opposingPlaneswalker = addPlaneswalker(player2, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveLandfall(new Forest());
        harness.assertLife(player1, 24);

        resolveLandfall(new Forest());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        resolveLandfall(new Forest());
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(opposingPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);

        resolveLandfall(new Forest());
        harness.assertLife(player2, 16);
        assertThat(opposingPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    private void resolveLandfall(Card land) {
        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        harness.setHand(player1, List.of(land));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent planeswalker = new Permanent(new AjaniOutlandChaperone());
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
