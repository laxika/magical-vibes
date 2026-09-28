package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoliathTruck.class, GrizzlyBears.class})
class GoliathTruckTest extends BaseCardTest {

    @Test
    void stowagePutsTwoCountersOnAnotherAttackingCreature() {
        Permanent truck = addReady(new GoliathTruck());
        Permanent crew = addReady(new GrizzlyBears());
        Permanent attacker = addReady(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, crew.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(truck.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(crew.isTapped()).isTrue();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void stowageCannotTargetTheTruckOrANonattackingCreature() {
        Permanent truck = addReady(new GoliathTruck());
        Permanent crew = addReady(new GrizzlyBears());
        Permanent attacker = addReady(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, crew.getId());
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0, 2));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, truck.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, crew.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
