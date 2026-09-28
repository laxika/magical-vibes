package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernoProject.class, Divination.class, Forest.class, GrizzlyBears.class, Shock.class})
class InfernoProjectTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with counters equal to the total mana value of own instants and sorceries")
    void entersWithCountersForOwnInstantsAndSorceries() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Forest(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Divination()));

        Permanent project = harness.enterBattlefieldAndReturn(player1, new InfernoProject());

        assertThat(project.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enters with no counters when the controller has no instants or sorceries in their graveyard")
    void entersWithNoCountersWithoutOwnInstantsOrSorceries() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new Forest(), creature));

        Permanent project = harness.enterBattlefieldAndReturn(player1, new InfernoProject());

        assertThat(project.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
