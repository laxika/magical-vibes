package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HawkeyeAvengingArcher.class, GrizzlyBears.class, Forest.class})
class HawkeyeAvengingArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when damage kills an opponent's creature")
    void drawsWhenDamagedOpponentCreatureDies() {
        harness.setHand(player1, List.of());
        Permanent hawkeye = addReadyHawkeye();
        Permanent target = addOneToughnessCreature(player2);
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(hawkeye.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not draw when damage kills a creature its controller controls")
    void doesNotDrawWhenDamagedAllyCreatureDies() {
        harness.setHand(player1, List.of());
        addReadyHawkeye();
        Permanent target = addOneToughnessCreature(player1);
        Forest notDrawn = new Forest();
        harness.setLibrary(player1, List.of(notDrawn));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deals 1 damage to a player when tapped")
    void dealsDamageToPlayer() {
        addReadyHawkeye();
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    private Permanent addReadyHawkeye() {
        return addCreatureReady(player1, new HawkeyeAvengingArcher());
    }

    private Permanent addOneToughnessCreature(com.github.laxika.magicalvibes.model.Player player) {
        GrizzlyBears card = new GrizzlyBears();
        card.setToughness(1);
        return addCreatureReady(player, card);
    }
}
