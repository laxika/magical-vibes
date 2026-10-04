package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.cards.m.ManorSkeleton;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrimgrinCorpseBorn.class, AmbushViper.class, ManorSkeleton.class})
class GrimgrinCorpseBornTest extends BaseCardTest {


    @Test
    @DisplayName("Tapped Grimgrin does not untap during controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent grimgrin = addGrimgrinReady(player1);
        grimgrin.tap();

        harness.performUntapStep(player1);

        assertThat(grimgrin.isTapped()).isTrue();
    }


    @Test
    @DisplayName("Sacrificing another creature untaps Grimgrin and adds a +1/+1 counter")
    void sacrificeCreatureUntapsAndAddsCounter() {
        Permanent grimgrin = addGrimgrinReady(player1);
        grimgrin.tap();
        addCreatureReady(player1, new AmbushViper());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Bears should be sacrificed
        harness.assertNotOnBattlefield(player1, "Ambush Viper");
        harness.assertInGraveyard(player1, "Ambush Viper");

        // Grimgrin should be untapped
        assertThat(grimgrin.isTapped()).isFalse();

        // Grimgrin should have a +1/+1 counter
        assertThat(grimgrin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot sacrifice Grimgrin to its own ability (excludeSelf)")
    void cannotSacrificeItself() {
        addGrimgrinReady(player1);
        // No other creatures — Grimgrin is the only creature

        // Should not be able to activate since there is no other creature to sacrifice
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing multiple creatures accumulates +1/+1 counters")
    void multipleActivationsAccumulateCounters() {
        Permanent grimgrin = addGrimgrinReady(player1);
        grimgrin.tap();
        addCreatureReady(player1, new AmbushViper());
        addCreatureReady(player1, new AmbushViper());

        // First activation
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Ambush Viper").getId());
        harness.passBothPriorities();

        assertThat(grimgrin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Tap again for second activation
        grimgrin.tap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(grimgrin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability works while untapped too (no tap cost)")
    void abilityWorksWhileUntapped() {
        Permanent grimgrin = addGrimgrinReady(player1);
        // Grimgrin starts untapped
        assertThat(grimgrin.isTapped()).isFalse();
        addCreatureReady(player1, new AmbushViper());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should still get the counter even when already untapped
        assertThat(grimgrin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }


    @Test
    @DisplayName("Attacking with Grimgrin queues targeted attack trigger for target selection")
    void attackTriggerQueuesForTargetSelection() {
        addGrimgrinReady(player1);
        addCreatureReady(player2, new AmbushViper());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Resolving attack trigger destroys the chosen creature and adds a counter")
    void attackTriggerDestroysCreatureAndAddsCounter() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent grimgrin = addGrimgrinReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new AmbushViper());

        declareAttackers(player1, List.of(0));

        // Choose the opponent's creature as target
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Opponent's creature should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentCreature.getId()));

        // Grimgrin should have a +1/+1 counter
        assertThat(grimgrin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attack trigger cannot target own creatures")
    void attackTriggerCannotTargetOwnCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addGrimgrinReady(player1);
        Permanent ownCreature = addCreatureReady(player1, new AmbushViper());
        addCreatureReady(player2, new AmbushViper());

        declareAttackers(player1, List.of(0));

        // Choosing own creature should fail
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.handlePermanentChosen(player1, ownCreature.getId())
        ).isInstanceOf(IllegalStateException.class);
    }


    @Test
    void entersTapped() {
        harness.castFromHand(player1, new GrimgrinCorpseBorn(), "{3}{U}{B}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grimgrin, Corpse-Born").isTapped()).isTrue();
    }

    @Test
    void canActivateWhileSummoningSickAndPaysSacrificeBeforeResolution() {
        Permanent grimgrin = harness.enterBattlefieldAndReturn(player1, new GrimgrinCorpseBorn());
        harness.addToBattlefield(player1, new AmbushViper());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Ambush Viper");
        assertThat(grimgrin.isTapped()).isTrue();
        assertThat(grimgrin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(grimgrin.isTapped()).isFalse();
        assertThat(grimgrin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void attackWithoutLegalTargetDoesNotAddCounter() {
        Permanent grimgrin = addGrimgrinReady(player1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(grimgrin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void attackTargetLeavingBattlefieldPreventsCounter() {
        Permanent grimgrin = addGrimgrinReady(player1);
        Permanent victim = addCreatureReady(player2, new AmbushViper());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        gd.playerBattlefields.get(player2.getId()).remove(victim);
        gd.playerGraveyards.get(player2.getId()).add(victim.getCard());
        harness.passBothPriorities();

        assertThat(grimgrin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void regeneratingAttackTargetStillAddsCounter() {
        Permanent grimgrin = addGrimgrinReady(player1);
        Permanent skeleton = addCreatureReady(player2, new ManorSkeleton());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, skeleton.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Manor Skeleton");
        assertThat(skeleton.getRegenerationShield()).isZero();
        assertThat(grimgrin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addGrimgrinReady(Player player) {
        return addCreatureReady(player, new GrimgrinCorpseBorn());
    }
}
