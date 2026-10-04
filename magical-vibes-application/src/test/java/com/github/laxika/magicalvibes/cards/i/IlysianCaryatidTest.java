package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.cards.v.VoraciousTyphon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IlysianCaryatid.class, NyxbornColossus.class, VoraciousTyphon.class})
class IlysianCaryatidTest extends BaseCardTest {

    @Test
    void addsOneManaWhenNoControlledCreatureHasPowerFour() {
        addCreatureReady(player1, new IlysianCaryatid());
        harness.addToBattlefield(player2, new NyxbornColossus());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void addsTwoManaOfOneColorWhenControllingCreatureWithPowerFour() {
        addCreatureReady(player1, new IlysianCaryatid());
        harness.addToBattlefield(player1, new NyxbornColossus());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void addsTwoManaWhenControlledCreatureHasExactlyFourPower() {
        var caryatid = addCreatureReady(player1, new IlysianCaryatid());
        harness.addToBattlefield(player1, new VoraciousTyphon());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(caryatid.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void addsOneManaWhenCaryatidHasOnlyThreePower() {
        var caryatid = addCreatureReady(player1, new IlysianCaryatid());
        caryatid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void caryatidItselfQualifiesWithThreePlusOnePlusOneCounters() {
        var caryatid = addCreatureReady(player1, new IlysianCaryatid());
        caryatid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
