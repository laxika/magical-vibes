package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvigoratingHotSpring.class, GrizzlyBears.class})
class InvigoratingHotSpringTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four +1/+1 counters")
    void entersWithFourCounters() {
        Permanent spring = castSpring();

        assertThat(spring.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gives haste to modified creatures you control")
    void givesHasteToModifiedCreatures() {
        harness.addToBattlefield(player1, new InvigoratingHotSpring());
        Permanent modified = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent unmodified = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, modified, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unmodified, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Removes a counter to put one on a creature you control")
    void movesCounterToControlledCreature() {
        Permanent spring = castSpring();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(spring.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate only once each turn and cannot target an opponent's creature")
    void activationRestrictions() {
        Permanent spring = castSpring();
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.activateAbility(player1, 0, null, ownTarget.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(spring.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private Permanent castSpring() {
        InvigoratingHotSpring springCard = new InvigoratingHotSpring();
        harness.setHand(player1, List.of(springCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, springCard, "{1}{R}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Invigorating Hot Spring");
    }
}
