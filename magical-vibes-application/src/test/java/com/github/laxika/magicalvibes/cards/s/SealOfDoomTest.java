package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MoggToady;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SealOfDoom.class, MoggToady.class, SpinelessThug.class})
class SealOfDoomTest extends BaseCardTest {

    @Test
    void activatingAbilitySacrificesSealAndPutsAbilityOnStack() {
        Permanent seal = harness.addToBattlefieldAndReturn(player1, new SealOfDoom());
        Permanent target = addCreatureReady(player2, new MoggToady());

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
        harness.addToBattlefield(player1, new SealOfDoom());
        Permanent target = addCreatureReady(player2, new MoggToady());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void destroysCreatureEvenWithRegenerationShield() {
        harness.addToBattlefield(player1, new SealOfDoom());
        Permanent target = addCreatureReady(player2, new MoggToady());
        target.setRegenerationShield(1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player1, new SealOfDoom());
        Permanent target = addCreatureReady(player2, new SpinelessThug());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new SealOfDoom());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SealOfDoom());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

}
