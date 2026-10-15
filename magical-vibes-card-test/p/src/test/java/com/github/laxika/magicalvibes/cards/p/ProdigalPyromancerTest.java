package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.r.ReveredDead;
import com.github.laxika.magicalvibes.cards.s.SimianSpiritGuide;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProdigalPyromancer.class, ReveredDead.class, SimianSpiritGuide.class, ChandraNalaar.class})
class ProdigalPyromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        ProdigalPyromancer card = new ProdigalPyromancer();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isSameAs(card);
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new ProdigalPyromancer()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Prodigal Pyromancer");
    }

    @Test
    @DisplayName("Activating ability targeting player puts it on the stack")
    void activatingTargetingPlayerPutsOnStack() {
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(pyromancer.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isSameAs(pyromancer.getCard());
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, destroying a 1/1")
    void deals1DamageDestroying1Toughness() {
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ReveredDead());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Revered Dead");
        harness.assertInGraveyard(player2, "Revered Dead");
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, 2/2 creature survives")
    void deals1DamageDoesNotKill2Toughness() {
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SimianSpiritGuide());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Simian Spirit Guide");
    }

    @Test
    @DisplayName("Deals 1 damage to target planeswalker")
    void deals1DamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        addCreatureReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        pyromancer.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetCreatureRemoved() {
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SimianSpiritGuide());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can deal damage to its controller")
    void canDamageItsController() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Can target itself and die from its own damage")
    void canTargetItself() {
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, 0, null, pyromancer.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Prodigal Pyromancer");
        harness.assertInGraveyard(player1, "Prodigal Pyromancer");
    }

    @Test
    @DisplayName("Ability resolves after its source dies in response")
    void abilityResolvesAfterSourceDies() {
        harness.setLife(player2, 20);
        Permanent source = addCreatureReady(player1, new ProdigalPyromancer());
        addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player2, 0, null, source.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Prodigal Pyromancer");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

}

