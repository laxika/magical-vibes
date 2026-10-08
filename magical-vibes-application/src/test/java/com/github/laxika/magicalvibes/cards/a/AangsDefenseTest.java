package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AangsDefense.class, Forest.class, GrizzlyBears.class})
class AangsDefenseTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your blocking creature and draws a card")
    void boostsBlockingCreatureAndDraws() {
        Permanent blocker = addBlockingCreature(player1);
        setupDefense();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(blocker.getPowerModifier()).isEqualTo(2);
        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Aang's Defense");
    }

    @Test
    @DisplayName("Cannot target an opponent's blocking creature")
    void cannotTargetOpponentsBlockingCreature() {
        Permanent blocker = addBlockingCreature(player2);
        setupDefense();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        setupDefense();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking");
    }

    @Test
    @DisplayName("Does not resolve if the target stops blocking")
    void fizzlesIfTargetStopsBlocking() {
        Permanent blocker = addBlockingCreature(player1);
        setupDefense();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castInstant(player1, 0, blocker.getId());
        blocker.setBlocking(false);
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        Permanent blocker = addBlockingCreature(player1);
        setupDefense();

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A tapped blocking creature remains a legal target")
    void canTargetTappedBlocker() {
        Permanent blocker = addBlockingCreature(player1);
        blocker.setTapped(true);
        setupDefense();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(blocker.getPowerModifier()).isEqualTo(2);
        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not draw if the target leaves the battlefield before resolution")
    void doesNotDrawIfTargetLeavesBattlefield() {
        Permanent blocker = addBlockingCreature(player1);
        setupDefense();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castInstant(player1, 0, blocker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(blocker);
        gd.playerGraveyards.get(player1.getId()).add(blocker.getCard());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Aang's Defense");
    }

    @Test
    @DisplayName("The boost persists after the creature stops blocking at end of combat")
    void boostPersistsAfterCombat() {
        Permanent blocker = addBlockingCreature(player1);
        setupDefense();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castAndResolveInstant(player1, 0, blocker.getId());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(blocker.getPowerModifier()).isEqualTo(2);
        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a surviving blocker after the blocked attacker dies in combat")
    void canTargetBlockerAfterAttackerDies() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        Permanent blocker = addBlockingCreature(player1);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTargetId(attacker.getId());
        setupDefense();
        harness.setHand(player1, List.of(new AangsDefense(), new AangsDefense()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.castAndResolveInstant(player1, 0, blocker.getId());
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(blocker.getPowerModifier()).isEqualTo(4);
        assertThat(blocker.getToughnessModifier()).isEqualTo(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void setupDefense() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AangsDefense()));
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private Permanent addBlockingCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = addCreatureReady(player, new GrizzlyBears());
        creature.setBlocking(true);
        return creature;
    }
}
