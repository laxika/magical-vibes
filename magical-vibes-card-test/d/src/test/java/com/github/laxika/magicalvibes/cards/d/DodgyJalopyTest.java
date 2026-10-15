package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DodgyJalopy.class, AirElemental.class, GrizzlyBears.class, Mountain.class, Terror.class})
class DodgyJalopyTest extends BaseCardTest {

    @Test
    void powerIsGreatestManaValueAmongYourCreatures() {
        Permanent jalopy = harness.addToBattlefieldAndReturn(player1, new DodgyJalopy());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, jalopy)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, jalopy)).isEqualTo(5);
    }

    @Test
    void crewTurnsJalopyIntoACreature() {
        Permanent jalopy = harness.addToBattlefieldAndReturn(player1, new DodgyJalopy());
        Permanent crewer = addCreatureReady(player1, new AirElemental());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, jalopy)).isTrue();
        assertThat(crewer.isTapped()).isTrue();
    }

    @Test
    void scavengeUsesTheJalopysDynamicPower() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.setGraveyard(player1, List.of(new DodgyJalopy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        harness.assertNotInGraveyard(player1, "Dodgy Jalopy");
    }

    @Test
    void scavengeRequiresACreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setGraveyard(player1, List.of(new DodgyJalopy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void scavengeKeepsPowerFromBeforeExilingWhenHighestManaValueCreatureDies() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent elemental = addCreatureReady(player1, new AirElemental());
        harness.setGraveyard(player1, List.of(new DodgyJalopy()));
        harness.setHand(player2, List.of(new Terror()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.assertNotInGraveyard(player1, "Dodgy Jalopy");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elemental.getId());
        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void scavengeDoesNotIncreaseWhenHigherManaValueCreatureEntersAfterActivation() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new DodgyJalopy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.addToBattlefield(player1, new AirElemental());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void scavengeCanTargetAnOpponentsCreatureAndUsesOnlyYourCreatures() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new DodgyJalopy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void scavengeAddsNoCountersWhenYouControlNoCreatures() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(new DodgyJalopy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Dodgy Jalopy");
    }

    @Test
    void scavengeCannotBeActivatedOutsideAMainPhase() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new DodgyJalopy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Dodgy Jalopy");
    }

    @Test
    void powerIsZeroWithoutControlledCreaturesAndIgnoresOpponentsCreatures() {
        Permanent jalopy = harness.addToBattlefieldAndReturn(player1, new DodgyJalopy());
        harness.addToBattlefield(player2, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, jalopy)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, jalopy)).isEqualTo(5);
    }

    @Test
    void crewedJalopyCountsItsOwnManaValueAfterOtherCreaturesLeave() {
        Permanent jalopy = harness.addToBattlefieldAndReturn(player1, new DodgyJalopy());
        Permanent elemental = addCreatureReady(player1, new AirElemental());
        harness.setHand(player1, List.of(new Terror()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, jalopy)).isEqualTo(5);
        harness.castAndResolveInstant(player1, 0, elemental.getId());

        assertThat(gqs.isCreature(gd, jalopy)).isTrue();
        assertThat(gqs.getEffectivePower(gd, jalopy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, jalopy)).isEqualTo(5);
    }

    @Test
    void powerDefiningAbilityWorksInGraveyard() {
        DodgyJalopy jalopy = new DodgyJalopy();
        harness.setGraveyard(player1, List.of(jalopy));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());

        assertThat(gqs.getEffectiveCardPower(gd, jalopy)).isEqualTo(2);
        harness.addToBattlefield(player1, new AirElemental());
        assertThat(gqs.getEffectiveCardPower(gd, jalopy)).isEqualTo(5);
    }

    @Test
    void crewCannotBePaidWithLessThanThreePower() {
        harness.addToBattlefield(player1, new DodgyJalopy());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void crewedJalopyTramplesOverCreatureBlocker() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DodgyJalopy());
        addCreatureReady(player1, new AirElemental());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Dodgy Jalopy");
    }
}
