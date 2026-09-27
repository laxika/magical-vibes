package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrokersConfluence.class, GrizzlyBears.class, RodOfRuin.class})
class BrokersConfluenceTest extends BaseCardTest {

    @Test
    void proliferatesThreeTimesWhenThatModeIsRepeated() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        cast(new int[]{0, 0, 0}, List.of());
        harness.passBothPriorities();
        proliferate(creature);
        proliferate(creature);
        proliferate(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void phasesOutTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{1, 1, 1}, List.of(creature.getId(), creature.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
    }

    @Test
    void countersTargetActivatedAbility() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        cast(new int[]{2, 2, 2}, List.of(rod.getId(), rod.getId(), rod.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetedModesRejectIllegalTargets() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());

        assertThatThrownBy(() -> cast(new int[]{1, 0, 0}, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modeIndices, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new BrokersConfluence()));
        addMana(player1);
        gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, modeIndices),
                null, null, targetIds, List.of());
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
    }

    private void proliferate(Permanent permanent) {
        harness.handleMultiplePermanentsChosen(player1, List.of(permanent.getId()));
    }
}
