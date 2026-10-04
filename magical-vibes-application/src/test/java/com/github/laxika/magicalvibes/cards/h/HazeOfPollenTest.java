package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.PouncingCheetah;
import com.github.laxika.magicalvibes.cards.e.Electrify;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HazeOfPollen.class, PouncingCheetah.class, Electrify.class})
class HazeOfPollenTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all combat damage after resolving")
    void preventsAllCombatDamage() {
        harness.setHand(player1, List.of(new HazeOfPollen()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("An unblocked attacker deals no combat damage while Haze of Pollen is in effect")
    void unblockedAttackerDealsNoDamage() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HazeOfPollen()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        Permanent attacker = addCreatureReady(player2, new PouncingCheetah());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new HazeOfPollen()));
        harness.setLibrary(player1, List.of(new PouncingCheetah()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Haze of Pollen");
        harness.assertInHand(player1, "Pouncing Cheetah");
    }

    @Test
    void preventsDamageToAttackersAndBlockers() {
        Permanent attacker = addCreatureReady(player1, new PouncingCheetah());
        Permanent blocker = addCreatureReady(player2, new PouncingCheetah());
        harness.setHand(player2, List.of(new HazeOfPollen()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        declareAttackers(player1, List.of(0));
        harness.castAndResolveInstant(player2, 0);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    void doesNotPreventNoncombatDamage() {
        Permanent creature = addCreatureReady(player2, new PouncingCheetah());
        harness.setHand(player1, List.of(new HazeOfPollen(), new Electrify()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Pouncing Cheetah");
        harness.assertInGraveyard(player2, "Pouncing Cheetah");
    }

    @Test
    void combatDamageResumesNextTurn() {
        harness.setHand(player1, List.of(new HazeOfPollen()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        addCreatureReady(player2, new PouncingCheetah());
        harness.setLife(player1, 20);
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    void cyclingPaysDiscardBeforeDrawingAndDoesNotPreventCombatDamage() {
        harness.setHand(player1, List.of(new HazeOfPollen()));
        harness.setLibrary(player1, List.of(new PouncingCheetah()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Haze of Pollen");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Pouncing Cheetah");

        addCreatureReady(player2, new PouncingCheetah());
        harness.setLife(player1, 20);
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    void cyclingRequiresThreeMana() {
        harness.setHand(player1, List.of(new HazeOfPollen()));
        harness.setLibrary(player1, List.of(new PouncingCheetah()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Haze of Pollen");
        harness.assertNotInGraveyard(player1, "Haze of Pollen");
        assertThat(gd.stack).isEmpty();
    }
}
