package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.m.MyrConvert;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForgehammerCenturion.class, GrizzlyBears.class, MindStone.class, MyrConvert.class,
        Naturalize.class, Shock.class, TreetopVillage.class})
class ForgehammerCenturionTest extends BaseCardTest {

    @Test
    @DisplayName("Gets an oil counter when a creature or artifact you control dies")
    void getsOilCounterWhenOwnCreatureOrArtifactDies() {
        Permanent centurion = addCenturion();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();
        assertThat(centurion.getCounterCount(CounterType.OIL)).isEqualTo(1);

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, artifact.getId());
        resolveAllTriggers();

        assertThat(centurion.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature")
    void ignoresOpponentCreature() {
        Permanent centurion = addCenturion();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, opponentCreature.getId());
        resolveAllTriggers();

        assertThat(centurion.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("May remove two oil counters on attack to stop a creature from blocking")
    void paysOilCountersOnAttack() {
        Permanent centurion = addCenturion();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        centurion.setCounterCount(CounterType.OIL, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(centurion.getCounterCount(CounterType.OIL)).isZero();
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(target.isCantBlockThisTurn()).isFalse();
        resolveAllTriggers();

        assertThat(centurion.getCounterCount(CounterType.OIL)).isZero();
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot pay the attack ability with fewer than two oil counters")
    void cannotPayWithOneOilCounter() {
        Permanent centurion = addCenturion();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        centurion.setCounterCount(CounterType.OIL, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(centurion.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The blocking restriction wears off at end of turn")
    void restrictionWearsOffAtEndOfTurn() {
        Permanent centurion = addCenturion();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        centurion.setCounterCount(CounterType.OIL, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Declining the attack payment preserves oil and creates no blocking restriction")
    void declinesOilPayment() {
        Permanent centurion = addCenturion();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        centurion.setCounterCount(CounterType.OIL, 3);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(centurion.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An artifact creature dying gives only one oil counter")
    void artifactCreatureTriggersOnlyOnce() {
        Permanent centurion = addCenturion();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MyrConvert());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(centurion.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("A controlled creature owned by the opponent still gives an oil counter")
    void controlledOpponentOwnedCreatureTriggers() {
        Permanent centurion = addCenturion();
        GrizzlyBears stolenCard = new GrizzlyBears();
        stolenCard.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, stolenCard);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(stolenCard);
        assertThat(centurion.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("An owned creature controlled by the opponent gives no oil counter")
    void ownedOpponentControlledCreatureDoesNotTrigger() {
        Permanent centurion = addCenturion();
        GrizzlyBears stolenCard = new GrizzlyBears();
        stolenCard.setOwnerId(player1.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, stolenCard);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(stolenCard);
        assertThat(centurion.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("A land that was a creature immediately before dying gives an oil counter")
    void animatedLandDeathTriggers() {
        Permanent centurion = addCenturion();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, land.getId());
        harness.castAndResolveInstant(player1, 0, land.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(centurion.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    private Permanent addCenturion() {
        return addCreatureReady(player1, new ForgehammerCenturion());
    }
}
