package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SuncrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Sunburst puts one +1/+1 counter on Suncrusher for each color spent")
    void sunburstCountsDistinctColorsSpent() {
        harness.setHand(player1, List.of(new Suncrusher()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent suncrusher = findPermanent(player1, "Suncrusher");
        assertThat(suncrusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Suncrusher destroys a target creature after removing a +1/+1 counter")
    void destroysTargetCreature() {
        Permanent suncrusher = addReadySuncrusher(player1, 1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        prepareTurn();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(suncrusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Suncrusher cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addReadySuncrusher(player1, 1);
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        prepareTurn();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Suncrusher can remove a +1/+1 counter to return itself to its owner's hand")
    void returnsItselfToHand() {
        Permanent suncrusher = addReadySuncrusher(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareTurn();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(suncrusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player1, "Suncrusher");
        harness.assertInHand(player1, "Suncrusher");
    }

    private Permanent addReadySuncrusher(Player player, int counters) {
        Permanent suncrusher = new Permanent(new Suncrusher());
        suncrusher.setSummoningSick(false);
        suncrusher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        gd.playerBattlefields.get(player.getId()).add(suncrusher);
        return suncrusher;
    }

    private void prepareTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
