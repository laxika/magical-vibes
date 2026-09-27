package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StunningShot.class, GrizzlyBears.class})
class StunningShotTest extends BaseCardTest {

    @Test
    void putsCountersOnYourCreatureAndStunsAnOpponentsCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(ownCreature.getId(), opposingCreature.getId()));

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opposingCreature.isTapped()).isTrue();
        assertThat(opposingCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void mayChooseNoTargets() {
        cast(List.of());

        harness.assertInGraveyard(player1, "Stunning Shot");
    }

    @Test
    void enforcesTargetControllers() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StunningShot()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(opposingCreature.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new StunningShot()));
        addMana();
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
