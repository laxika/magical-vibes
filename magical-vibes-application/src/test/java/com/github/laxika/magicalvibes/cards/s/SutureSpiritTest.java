package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LoyalGyrfalcon;
import com.github.laxika.magicalvibes.cards.r.RuggedPrairie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SutureSpirit.class, LoyalGyrfalcon.class, RuggedPrairie.class})
class SutureSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts a regeneration ability on the stack targeting the creature")
    void activatingTargetsCreature() {
        harness.addToBattlefield(player1, new SutureSpirit());
        Permanent creature = addCreatureReady(player1, new LoyalGyrfalcon());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield to the target creature")
    void resolvingGrantsShield() {
        harness.addToBattlefield(player1, new SutureSpirit());
        Permanent creature = addCreatureReady(player1, new LoyalGyrfalcon());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new SutureSpirit());
        Permanent creature = addCreatureReady(player1, new LoyalGyrfalcon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new SutureSpirit());
        Permanent creature = addCreatureReady(player2, new LoyalGyrfalcon());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new SutureSpirit());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RuggedPrairie());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(3);
    }
}
