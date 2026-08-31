package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JoinTheRanks;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvatarEnthusiasts.class, JoinTheRanks.class, GrizzlyBears.class})
class AvatarEnthusiastsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself for each Ally that enters under your control")
    void putsCounterForEachAllyEntering() {
        Permanent avatarEnthusiasts = harness.addToBattlefieldAndReturn(player1, new AvatarEnthusiasts());

        harness.setHand(player1, List.of(new JoinTheRanks()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(avatarEnthusiasts.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a non-Ally creature")
    void doesNotTriggerForNonAlly() {
        Permanent avatarEnthusiasts = harness.addToBattlefieldAndReturn(player1, new AvatarEnthusiasts());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(avatarEnthusiasts.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when Avatar Enthusiasts itself enters")
    void doesNotTriggerForItsOwnEntry() {
        harness.setHand(player1, List.of(new AvatarEnthusiasts()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Avatar Enthusiasts")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger for an Ally entering under an opponent's control")
    void doesNotTriggerForOpponentsAlly() {
        Permanent avatarEnthusiasts = harness.addToBattlefieldAndReturn(player1, new AvatarEnthusiasts());

        harness.setHand(player2, List.of(new JoinTheRanks()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(avatarEnthusiasts.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
