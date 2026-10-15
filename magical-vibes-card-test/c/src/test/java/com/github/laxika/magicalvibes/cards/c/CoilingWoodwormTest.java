package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoilingWoodworm.class, Forest.class, Mountain.class})
class CoilingWoodwormTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of Forests on the battlefield; toughness stays 1")
    void powerEqualsForestsOnBattlefield() {
        Permanent woodworm = addCreatureReady(player1, new CoilingWoodworm());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        assertThat(gqs.getEffectivePower(gd, woodworm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, woodworm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power updates as Forests enter and leave the battlefield")
    void powerUpdatesWhenForestsChange() {
        Permanent woodworm = addCreatureReady(player1, new CoilingWoodworm());

        assertThat(gqs.getEffectivePower(gd, woodworm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, woodworm)).isEqualTo(1);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        assertThat(gqs.getEffectivePower(gd, woodworm)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Forest"));
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getCard().getName().equals("Forest"));
        assertThat(gqs.getEffectivePower(gd, woodworm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, woodworm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Forests outside the battlefield do not contribute to power")
    void forestsInOtherZonesDoNotCount() {
        Permanent woodworm = addCreatureReady(player1, new CoilingWoodworm());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setExile(player2, List.of(new Forest()));

        assertThat(gqs.getEffectivePower(gd, woodworm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, woodworm)).isEqualTo(1);
    }

    @Test
    @DisplayName("The power-defining ability applies while Woodworm is in the graveyard")
    void powerIsDefinedInGraveyard() {
        CoilingWoodworm woodworm = new CoilingWoodworm();
        harness.setGraveyard(player1, List.of(woodworm));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectiveCardPower(gd, woodworm)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, woodworm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters increase both power and toughness after the defining ability")
    void countersApplyAfterDefiningAbility() {
        Permanent woodworm = addCreatureReady(player1, new CoilingWoodworm());
        harness.addToBattlefield(player2, new Forest());
        woodworm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.getEffectivePower(gd, woodworm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, woodworm)).isEqualTo(3);

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, woodworm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, woodworm)).isEqualTo(3);
    }
}
