package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WorldsoulColossus.class, VernadiShieldmate.class})
class WorldsoulColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with the chosen number of +1/+1 counters")
    void entersWithXPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new WorldsoulColossus()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 3);
        harness.passBothPriorities();

        Permanent colossus = findPermanent(player1, "Worldsoul Colossus");
        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Convoke taps a creature to help pay its cost")
    void castsWithConvoke() {
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        harness.setHand(player1, List.of(new WorldsoulColossus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 2, null, null, List.of(), List.of(convokeCreature.getId()));

        assertThat(convokeCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        Permanent colossus = findPermanent(player1, "Worldsoul Colossus");
        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Choosing X = 0 puts the counterless creature into the graveyard")
    void zeroXDiesAsAStateBasedAction() {
        harness.setHand(player1, List.of(new WorldsoulColossus()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Worldsoul Colossus");
        harness.assertInGraveyard(player1, "Worldsoul Colossus");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Convoke can pay the entire cost including X with summoning-sick creatures")
    void convokePaysColoredAndXCostsWithoutMana() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        third.setSummoningSick(true);
        harness.setHand(player1, List.of(new WorldsoulColossus()));

        gs.playCard(gd, player1, 0, 1, null, null, List.of(),
                List.of(first.getId(), second.getId(), third.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        harness.passBothPriorities();

        Permanent colossus = findPermanent(player1, "Worldsoul Colossus");
        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Worldsoul Colossus");
        assertThat(gd.stack).isEmpty();
    }
}
