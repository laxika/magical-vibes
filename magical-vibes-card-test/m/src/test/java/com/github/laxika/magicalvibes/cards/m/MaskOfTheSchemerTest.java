package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaskOfTheSchemer.class, HillGiant.class, GrizzlyBears.class, Forest.class, HardenedScales.class})
class MaskOfTheSchemerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping Mask of the Schemer attaches it to a creature")
    void equippingAttachesToCreature() {
        Permanent mask = addMaskReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mask.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Combat damage makes the equipped creature connive for the damage dealt")
    void combatDamageConnivesForDamageDealt() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        creature.setAttacking(true);
        Permanent mask = addMaskReady(player1);
        mask.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        resolveCombat(player1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Mask of the Schemer does not trigger for combat damage dealt only to a creature")
    void combatDamageToCreatureDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        creature.setAttacking(true);
        Permanent mask = addMaskReady(player1);
        mask.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        Permanent blocker = addCreatureReady(player2, new HillGiant());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addMaskReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new MaskOfTheSchemer());
    }

    @Test
    void landDiscardsDoNotAddCounters() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        creature.setAttacking(true);
        addMaskReady(player1).setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        resolveCombat(player1);
        harness.passBothPriorities();
        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void creatureControllerConnivesWhenEquipmentHasAnotherController() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        creature.setAttacking(true);
        addMaskReady(player2).setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, 0);
        }
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void changingAttachmentDoesNotChangeWhichCreatureConnives() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        creature.setAttacking(true);
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent mask = addMaskReady(player1);
        mask.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        resolveCombat(player1);
        mask.setAttachedTo(other.getId());
        harness.passBothPriorities();
        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void connivePutsAllCountersAtOnce() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        creature.setAttacking(true);
        addMaskReady(player1).setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new HardenedScales());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        resolveCombat(player1);
        harness.passBothPriorities();
        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

}
