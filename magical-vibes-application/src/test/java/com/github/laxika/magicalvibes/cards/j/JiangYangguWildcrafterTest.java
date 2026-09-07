package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JiangYangguWildcrafter.class, GrizzlyBears.class})
class JiangYangguWildcrafterTest extends BaseCardTest {

    @Test
    @DisplayName("-1 puts a +1/+1 counter on target creature")
    void minusOnePutsCounterOnTargetCreature() {
        Permanent yanggu = addReadyYanggu(player1, 3);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(yanggu.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A controlled creature with a +1/+1 counter gains the mana ability")
    void counteredControlledCreatureGainsManaAbility() {
        harness.addToBattlefield(player1, new JiangYangguWildcrafter());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int creatureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);

        harness.activateAbility(player1, creatureIndex, null, null);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures without a +1/+1 counter do not gain the mana ability")
    void creatureWithoutCounterDoesNotGainManaAbility() {
        harness.addToBattlefield(player1, new JiangYangguWildcrafter());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Opponents' countered creatures do not gain the mana ability")
    void opponentCreatureDoesNotGainManaAbility() {
        harness.addToBattlefield(player1, new JiangYangguWildcrafter());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    private Permanent addReadyYanggu(Player player, int loyalty) {
        Permanent perm = new Permanent(new JiangYangguWildcrafter());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
