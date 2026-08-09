package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SealOfDoomTest extends BaseCardTest {

    @Test
    void activatingAbilitySacrificesSealAndPutsAbilityOnStack() {
        Permanent seal = addReadySeal(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(seal);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(seal.getCard());
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    void resolvingAbilityDestroysTargetNonblackCreature() {
        addReadySeal(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void destroysCreatureEvenWithRegenerationShield() {
        addReadySeal(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setRegenerationShield(1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void cannotTargetBlackCreature() {
        addReadySeal(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = new Permanent(new MassOfGhouls());
        gd.playerBattlefields.get(player2.getId()).add(target);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    void cannotTargetLand() {
        addReadySeal(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = new Permanent(new Island());
        gd.playerBattlefields.get(player2.getId()).add(target);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    private Permanent addReadySeal(com.github.laxika.magicalvibes.model.Player player) {
        Permanent seal = new Permanent(new SealOfDoom());
        seal.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(seal);
        return seal;
    }
}
