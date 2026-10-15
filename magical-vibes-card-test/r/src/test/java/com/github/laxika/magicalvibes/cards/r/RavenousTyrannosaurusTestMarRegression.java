package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BasiliskCollar;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavenousTyrannosaurus.class, GrizzlyBears.class, BasiliskCollar.class,
        FurnaceOfRath.class, Unsummon.class})
class RavenousTyrannosaurusTestMarRegression extends BaseCardTest {

    @Test
    @DisplayName("Devouring two creatures gives six +1/+1 counters")
    void devourTwoAddsSixCounters() {
        Permanent fodderA = addCreatureReady(player1, new GrizzlyBears());
        Permanent fodderB = addCreatureReady(player1, new GrizzlyBears());

        harness.castFromHand(player1, new RavenousTyrannosaurus(), "{4}{R}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodderA.getId(), fodderB.getId()));

        Permanent tyrannosaurus = findPermanent(player1, "Ravenous Tyrannosaurus");
        assertThat(tyrannosaurus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
    }

    @Test
    @DisplayName("The attack trigger deals excess damage to the target creature's controller")
    void attackTriggerDealsExcessDamageToCreatureController() {
        Permanent tyrannosaurus = addCreatureReady(player1, new RavenousTyrannosaurus());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 30);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(tyrannosaurus.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The attack trigger cannot target Ravenous Tyrannosaurus itself")
    void attackTriggerCannotTargetItself() {
        Permanent tyrannosaurus = addCreatureReady(player1, new RavenousTyrannosaurus());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, tyrannosaurus.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack trigger can resolve without a target")
    void attackTriggerCanResolveWithoutTarget() {
        addCreatureReady(player1, new RavenousTyrannosaurus());
        harness.setLife(player2, 30);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ravenous Tyrannosaurus");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
    }

    @Test
    void deathtouchNeedsOnlyOneDamageBeforeExcess() {
        Permanent source = addCreatureReady(player1, new RavenousTyrannosaurus());
        Permanent collar = harness.addToBattlefieldAndReturn(player1, new BasiliskCollar());
        collar.setAttachedTo(source.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 30);

        resolveAttackTrigger(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 25);
    }

    @Test
    void damageDoublerAppliesAfterExcessIsDetermined() {
        addCreatureReady(player1, new RavenousTyrannosaurus());
        harness.addToBattlefield(player1, new FurnaceOfRath());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 30);

        resolveAttackTrigger(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 22);
    }

    @Test
    void removedSourceUsesItsLastKnownPower() {
        Permanent source = addCreatureReady(player1, new RavenousTyrannosaurus());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 30);
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            harness.ensurePriority(player1);
            harness.castInstant(player1, 0, source.getId());
            harness.passBothPriorities();
            resolveAllTriggers();
        });

        harness.assertInHand(player1, "Ravenous Tyrannosaurus");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 23);
    }

    @Test
    void powerIsReadWhenTriggerResolves() {
        Permanent source = addCreatureReady(player1, new RavenousTyrannosaurus());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 30);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
            resolveAllTriggers();
        });

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 23);
    }

    @Test
    void removedTargetPreventsDamageToItsController() {
        addCreatureReady(player1, new RavenousTyrannosaurus());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 30);
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            harness.ensurePriority(player1);
            harness.castInstant(player1, 0, target.getId());
            harness.passBothPriorities();
            resolveAllTriggers();
        });

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertLife(player2, 30);
    }

    @Test
    void alreadyMarkedDamageIncreasesExcess() {
        addCreatureReady(player1, new RavenousTyrannosaurus());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setMarkedDamage(1);
        harness.setLife(player2, 30);

        resolveAttackTrigger(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 25);
    }

    @Test
    void canDamageOwnCreatureAndOwnController() {
        addCreatureReady(player1, new RavenousTyrannosaurus());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 30);

        resolveAttackTrigger(target);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 26);
    }

    @Test
    void devourCanSacrificeNothingWhenCreaturesAreAvailable() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new RavenousTyrannosaurus(), "{4}{R}{G}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(findPermanent(player1, "Ravenous Tyrannosaurus")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
    }

    private void resolveAttackTrigger(Permanent target) {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            resolveAllTriggers();
        });
    }
}
