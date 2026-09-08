package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaybladeTrooper.class, GrizzlyBears.class, Murder.class})
class RaybladeTrooperTest extends BaseCardTest {

    @Test
    void entersAndPutsCounterOnTargetCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RaybladeTrooper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RaybladeTrooper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void createsHumanSoldierWhenCounteredAllyDies() {
        harness.addToBattlefield(player1, new RaybladeTrooper());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyWithMurder(player2, player1, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
    }

    @Test
    void doesNotCreateHumanSoldierWhenAllyDiesWithoutPlusOneCounter() {
        harness.addToBattlefield(player1, new RaybladeTrooper());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroyWithMurder(player2, player1, bears.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
    }

    @Test
    void createsHumanSoldierWhenItselfDiesWithPlusOneCounter() {
        Permanent trooper = harness.addToBattlefieldAndReturn(player1, new RaybladeTrooper());
        trooper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyWithMurder(player2, player1, trooper.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
    }

    private void destroyWithMurder(Player caster, Player targetController, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);

        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
