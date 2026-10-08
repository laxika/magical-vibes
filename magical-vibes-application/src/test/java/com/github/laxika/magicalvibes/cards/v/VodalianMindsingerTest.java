package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.z.RoostOfDrakes;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VodalianMindsinger.class, LlanowarElves.class, HillGiant.class, GrizzlyBears.class,
        AirElemental.class, GiantGrowth.class, Incinerate.class, RoostOfDrakes.class})
class VodalianMindsingerTest extends BaseCardTest {

    @Test
    void entersWithoutKickerAndStealsSmallerCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new VodalianMindsinger()));
        addBaseMana();

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Vodalian Mindsinger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void redKickerAddsCountersAndAllowsStealingLargerCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new VodalianMindsinger()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0, target.getId());
        resolveAllTriggers();

        Permanent mindsinger = findPermanent(player1, "Vodalian Mindsinger");
        assertThat(mindsinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    void greenKickerAddsCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new VodalianMindsinger()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithRepeatedCosts(player1, 0, target.getId(), List.of("{1}{G}"));
        resolveAllTriggers();

        Permanent mindsinger = findPermanent(player1, "Vodalian Mindsinger");
        assertThat(mindsinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    void cannotTargetCreatureWithEqualPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new VodalianMindsinger()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, legalTarget.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void bothKickersAddFourCountersAndAllowStealingFourPowerCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new VodalianMindsinger()));
        addBaseMana();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, true, null, null, null, null, List.of("{1}{G}"), false);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Vodalian Mindsinger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    void greenKickerAllowsStealingThreePowerCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new VodalianMindsinger()));
        addBaseMana();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{G}"));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Vodalian Mindsinger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    void payingOnlyGreenKickerTriggersKickedSpellAbilities() {
        harness.addToBattlefield(player1, new RoostOfDrakes());
        harness.setHand(player1, List.of(new VodalianMindsinger()));
        addBaseMana();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{G}"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Drake")).hasSize(1);
        assertThat(findPermanent(player1, "Vodalian Mindsinger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void targetBecomingTooPowerfulInResponseIsNotStolen() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new VodalianMindsinger()));
        harness.setHand(player2, List.of(new GiantGrowth()));
        addBaseMana();
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void powerIncreaseAfterResolutionDoesNotEndControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new VodalianMindsinger(), new GiantGrowth()));
        addBaseMana();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    void sourceDyingBeforeTriggerResolvesPreventsControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new VodalianMindsinger()));
        harness.setHand(player2, List.of(new Incinerate()));
        addBaseMana();
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Vodalian Mindsinger"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Vodalian Mindsinger");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void sourceDyingAfterTriggerResolvesReturnsStolenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new VodalianMindsinger()));
        harness.setHand(player2, List.of(new Incinerate()));
        addBaseMana();
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Vodalian Mindsinger"));

        harness.assertInGraveyard(player1, "Vodalian Mindsinger");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void losingControlOfSourceReturnsStolenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new VodalianMindsinger()));
        addBaseMana();
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
        Permanent source = findPermanent(player1, "Vodalian Mindsinger");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new VodalianMindsinger()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castKickedCreature(player2, 0, source.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(source, target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source, target);
    }

    @Test
    void cannotPayGreenKickerTwice() {
        harness.setHand(player1, List.of(new VodalianMindsinger()));
        addBaseMana();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithRepeatedCosts(
                player1, 0, List.of("{1}{G}", "{1}{G}")))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Vodalian Mindsinger");
        harness.assertNotOnBattlefield(player1, "Vodalian Mindsinger");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
