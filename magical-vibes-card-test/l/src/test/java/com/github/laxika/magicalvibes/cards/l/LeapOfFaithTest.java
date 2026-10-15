package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeapOfFaith.class, GrizzlyBears.class, Mountain.class, Shock.class})
class LeapOfFaithTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gains flying")
    void grantsFlying() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(creature);

        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(creature.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Prevents all damage dealt to the target creature this turn")
    void preventsAllDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(creature);
        shock(creature);

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Prevention wears off after turn cleanup")
    void preventionWearsOff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(creature);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        shock(creature);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new LeapOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountainId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents repeated damage events throughout the turn")
    void preventsRepeatedDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(creature);
        shock(creature);
        shock(creature);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can grant flying and prevent damage to an opponent's creature")
    void protectsOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castResolve(creature);
        shock(creature);

        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not grant flying or prevent damage to another creature")
    void onlyProtectsTarget() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castResolve(protectedCreature);
        shock(otherCreature);

        assertThat(otherCreature.hasKeyword(Keyword.FLYING)).isFalse();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Prevents incoming combat damage without preventing outgoing damage")
    void preventsOnlyIncomingCombatDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setBlockedThisCombat(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        castResolve(attacker);
        harness.resolveCombatDamage();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not resolve when its target dies in response")
    void doesNotResolveWithMissingTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeapOfFaith(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.FLYING)).isFalse();
        harness.assertInGraveyard(player1, "Leap of Faith");
        assertThat(gd.stack).isEmpty();
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new LeapOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void shock(Permanent target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
