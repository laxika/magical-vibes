package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingmantleChaplain.class, WallOfVines.class, GrizzlyBears.class})
class WingmantleChaplainTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one Bird for each creature with defender you control")
    void entersAndCreatesBirdsForControlledDefenders() {
        harness.addToBattlefield(player1, new WallOfVines());
        castChaplain();

        assertThat(countPermanents(player1, "Bird")).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates a Bird when another creature with defender enters under your control")
    void createsBirdWhenAnotherDefenderEnters() {
        castChaplain();
        harness.setHand(player1, List.of(new WallOfVines()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird")).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a creature without defender")
    void doesNotTriggerForNonDefender() {
        castChaplain();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird")).isEqualTo(1);
    }

    private void castChaplain() {
        harness.setHand(player1, List.of(new WingmantleChaplain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
