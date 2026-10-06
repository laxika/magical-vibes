package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GreensideWatcher;
import com.github.laxika.magicalvibes.cards.b.BorosGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.m.Mugging;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShieldedPassage.class, GreensideWatcher.class, Mugging.class, BorosGuildgate.class})
class ShieldedPassageTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage dealt to the target creature this turn")
    void preventsAllDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreensideWatcher());

        castShieldedPassage(creature);
        castMugging(creature);

        harness.assertOnBattlefield(player1, "Greenside Watcher");
        harness.assertNotInGraveyard(player1, "Greenside Watcher");
    }

    @Test
    @DisplayName("Prevention wears off after the turn")
    void wearsOffAfterTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreensideWatcher());
        harness.setLibrary(player1, List.of(new BorosGuildgate(), new BorosGuildgate()));
        harness.setLibrary(player2, List.of(new BorosGuildgate(), new BorosGuildgate()));

        castShieldedPassage(creature);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        castMugging(creature);

        harness.assertInGraveyard(player1, "Greenside Watcher");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new BorosGuildgate());
        harness.setHand(player1, List.of(new ShieldedPassage()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID landId = land.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void preventsRepeatedDamageButDoesNotProtectOtherCreatures() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GreensideWatcher());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GreensideWatcher());

        castShieldedPassage(protectedCreature);
        castMugging(protectedCreature);
        castMugging(protectedCreature);
        castMugging(otherCreature);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature).doesNotContain(otherCreature);
        assertThat(protectedCreature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Greenside Watcher");
    }

    @Test
    void canProtectAnOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreensideWatcher());

        castShieldedPassage(creature);
        castMugging(creature);

        harness.assertOnBattlefield(player2, "Greenside Watcher");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    void preventsIncomingCombatDamageButAllowsOutgoingDamage() {
        Permanent attacker = addCreatureReady(player1, new GreensideWatcher());
        Permanent blocker = addCreatureReady(player2, new GreensideWatcher());
        castShieldedPassage(blocker);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player2, "Greenside Watcher");
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Greenside Watcher");
    }

    private void castShieldedPassage(Permanent target) {
        harness.setHand(player1, List.of(new ShieldedPassage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void castMugging(Permanent target) {
        harness.setHand(player1, List.of(new Mugging()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
