package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoggFanatic.class, GrizzlyBears.class, LlanowarElves.class, ChandraNalaar.class, Forest.class})
class MoggFanaticTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Mogg Fanatic puts it on the stack and resolves to battlefield")
    void castAndResolve() {
        harness.setHand(player1, List.of(new MoggFanatic()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Mogg Fanatic");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new MoggFanatic()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Activating ability — sacrifice and stack =====

    @Test
    @DisplayName("Activating ability sacrifices Mogg Fanatic and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        addReadyMoggFanatic(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        // Mogg Fanatic should be sacrificed (not on battlefield, in graveyard)
        harness.assertNotOnBattlefield(player1, "Mogg Fanatic");
        harness.assertInGraveyard(player1, "Mogg Fanatic");

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Can activate ability targeting a creature")
    void activatingTargetingCreaturePutsOnStack() {
        addReadyMoggFanatic(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    // ===== Dealing damage to player =====

    @Test
    @DisplayName("Deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyMoggFanatic(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Can target self to deal 1 damage")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        addReadyMoggFanatic(player1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    // ===== Dealing damage to creature =====

    @Test
    @DisplayName("Deals 1 damage to target creature, destroying a 1/1")
    void deals1DamageDestroying1Toughness() {
        addReadyMoggFanatic(player1);
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LlanowarElves()).getId();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, 2/2 creature survives")
    void deals1DamageDoesNotKill2Toughness() {
        addReadyMoggFanatic(player1);
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 1 damage to target planeswalker")
    void deals1DamageToPlaneswalker() {
        addReadyMoggFanatic(player1);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonplaneswalker permanent")
    void cannotTargetNonCreatureNonPlaneswalkerPermanent() {
        addReadyMoggFanatic(player1);
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== No tap required, no mana required =====

    @Test
    @DisplayName("Can activate without any mana")
    void canActivateWithoutMana() {
        addReadyMoggFanatic(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate even when tapped (no tap cost)")
    void canActivateWhenTapped() {
        Permanent mogg = addReadyMoggFanatic(player1);
        mogg.tap();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Summoning sickness =====

    @Test
    @DisplayName("Can activate with summoning sickness (no tap cost)")
    void canActivateWithSummoningSickness() {
        MoggFanatic card = new MoggFanatic();
        harness.addToBattlefield(player1, card);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetCreatureRemoved() {
        addReadyMoggFanatic(player1);
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.activateAbility(player1, 0, null, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    // ===== Mogg Fanatic goes to graveyard on sacrifice =====

    @Test
    @DisplayName("Mogg Fanatic is in graveyard after activation, even before ability resolves")
    void inGraveyardAfterActivation() {
        addReadyMoggFanatic(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        // Before resolution, Mogg Fanatic should already be in the graveyard
        harness.assertNotOnBattlefield(player1, "Mogg Fanatic");
        harness.assertInGraveyard(player1, "Mogg Fanatic");
    }

    @Test
    @DisplayName("Can target itself, but its ability fizzles after it is sacrificed")
    void canTargetItselfBeforePayingSacrificeCost() {
        Permanent mogg = harness.addToBattlefieldAndReturn(player1, new MoggFanatic());

        harness.activateAbility(player1, 0, null, mogg.getId());

        harness.assertInGraveyard(player1, "Mogg Fanatic");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can deal damage to a creature its controller controls")
    void canDamageFriendlyCreature() {
        harness.addToBattlefield(player1, new MoggFanatic());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mogg Fanatic");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Rejecting an illegal target does not sacrifice Mogg Fanatic")
    void illegalTargetDoesNotPaySacrificeCost() {
        harness.addToBattlefield(player1, new MoggFanatic());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mogg Fanatic");
        harness.assertNotInGraveyard(player1, "Mogg Fanatic");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyMoggFanatic(Player player) {
        return addCreatureReady(player, new MoggFanatic());
    }
}

