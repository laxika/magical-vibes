package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JiangYangguWildcrafter.class, PrimordialWurm.class})
class JiangYangguWildcrafterTest extends BaseCardTest {

    @Test
    @DisplayName("-1 puts a +1/+1 counter on target creature")
    void minusOnePutsCounterOnTargetCreature() {
        Permanent yanggu = addReadyYanggu(player1, 3);
        Permanent creature = addCreatureReady(player1, new PrimordialWurm());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(yanggu.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A controlled creature with a +1/+1 counter gains the mana ability")
    void counteredControlledCreatureGainsManaAbility() {
        harness.addToBattlefield(player1, new JiangYangguWildcrafter());
        Permanent creature = addCreatureReady(player1, new PrimordialWurm());
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
        addCreatureReady(player1, new PrimordialWurm());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Opponents' countered creatures do not gain the mana ability")
    void opponentCreatureDoesNotGainManaAbility() {
        harness.addToBattlefield(player1, new JiangYangguWildcrafter());
        Permanent creature = addCreatureReady(player2, new PrimordialWurm());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void minusOneCanTargetOpponentsCreature() {
        addReadyYanggu(player1, 3);
        Permanent creature = addCreatureReady(player2, new PrimordialWurm());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void newlyCounteredCreatureCanImmediatelyProduceMana() {
        addReadyYanggu(player1, 3);
        Permanent creature = addCreatureReady(player1, new PrimordialWurm());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantedTapAbilityRespectsSummoningSickness() {
        harness.addToBattlefield(player1, new JiangYangguWildcrafter());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        creature.setSummoningSick(true);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void losingLastCounterRemovesManaAbility() {
        harness.addToBattlefield(player1, new JiangYangguWildcrafter());
        Permanent creature = addCreatureReady(player1, new PrimordialWurm());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        creature.untap();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void finalLoyaltyActivationResolvesButDoesNotLeaveManaAbility() {
        addReadyYanggu(player1, 1);
        Permanent creature = addCreatureReady(player1, new PrimordialWurm());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jiang Yanggu, Wildcrafter");
        harness.assertInGraveyard(player1, "Jiang Yanggu, Wildcrafter");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void grantedAbilityProducesEachColorWithoutUsingStack(ManaColor color) {
        harness.addToBattlefield(player1, new JiangYangguWildcrafter());
        Permanent creature = addCreatureReady(player1, new PrimordialWurm());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void otherCounterTypesDoNotGrantManaAbility() {
        harness.addToBattlefield(player1, new JiangYangguWildcrafter());
        Permanent creature = addCreatureReady(player1, new PrimordialWurm());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }
    private Permanent addReadyYanggu(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new JiangYangguWildcrafter());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
