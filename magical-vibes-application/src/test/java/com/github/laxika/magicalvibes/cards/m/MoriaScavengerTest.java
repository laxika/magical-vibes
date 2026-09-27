package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoriaScavenger.class, GrizzlyBears.class, Shock.class})
class MoriaScavengerTest extends BaseCardTest {

    @Test
    void drawsAndAmassesWhenDiscardingAcreature() {
        Permanent scavenger = addScavenger();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Shock()));

        harness.activateAbility(player1, indexOf(scavenger), null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        Permanent army = findPermanent(player1, "Orc Army");
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.ORC, CardSubtype.ARMY);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void drawsAndDiscardsWithoutAmassingWhenDiscardingANoncreature() {
        Permanent scavenger = addScavenger();
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, indexOf(scavenger), null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
        assertThat(findPermanents(player1, "Orc Army")).isEmpty();
    }

    private Permanent addScavenger() {
        return addCreatureReady(player1, new MoriaScavenger());
    }

    private int indexOf(Permanent scavenger) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(scavenger);
    }
}
