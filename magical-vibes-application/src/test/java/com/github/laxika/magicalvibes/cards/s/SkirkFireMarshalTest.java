package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GoblinSledder;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkirkFireMarshal.class, GoblinSledder.class, GlorySeeker.class})
class SkirkFireMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 10 damage to each creature and each player, but survives its red damage")
    void dealsTenDamageToCreaturesAndPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent marshal = addCreatureReady(player1, new SkirkFireMarshal());
        addReadyGoblins(4);
        harness.addToBattlefield(player2, new GlorySeeker());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
        harness.assertOnBattlefield(player1, "Skirk Fire Marshal");
        assertThat(marshal.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Goblin Sledder");
        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        assertThat(marshal.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Only Goblins can be tapped to pay the ability's cost")
    void onlyGoblinsCanBeTappedForTheCost() {
        Permanent marshal = addCreatureReady(player1, new SkirkFireMarshal());
        List<Permanent> goblins = addReadyGoblins(5);
        Permanent nonGoblin = addCreatureReady(player1, new GlorySeeker());

        harness.activateAbility(player1, 0, null, null);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonGoblin.getId()))
                .isInstanceOf(IllegalStateException.class);
        for (Permanent goblin : goblins) {
            harness.handlePermanentChosen(player1, goblin.getId());
        }

        assertThat(marshal.isTapped()).isFalse();
        assertThat(nonGoblin.isTapped()).isFalse();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skirk Fire Marshal");
        harness.assertNotOnBattlefield(player1, "Goblin Sledder");
        harness.assertNotOnBattlefield(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Cannot activate without five untapped Goblins")
    void requiresFiveUntappedGoblins() {
        Permanent marshal = addCreatureReady(player1, new SkirkFireMarshal());
        addReadyGoblins(3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough");
        assertThat(marshal.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped Goblins cannot be used to pay the ability's cost")
    void cannotUseTappedGoblinAsPayment() {
        Permanent marshal = addCreatureReady(player1, new SkirkFireMarshal());
        List<Permanent> readyGoblins = addReadyGoblins(3);
        Permanent tappedGoblin = addCreatureReady(player1, new GoblinSledder());
        tappedGoblin.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough");
        assertThat(marshal.isTapped()).isFalse();
        assertThat(readyGoblins).allMatch(permanent -> !permanent.isTapped());
        assertThat(tappedGoblin.isTapped()).isTrue();
    }

    private List<Permanent> addReadyGoblins(int count) {
        List<Permanent> goblins = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            goblins.add(addCreatureReady(player1, new GoblinSledder()));
        }
        return goblins;
    }
}
