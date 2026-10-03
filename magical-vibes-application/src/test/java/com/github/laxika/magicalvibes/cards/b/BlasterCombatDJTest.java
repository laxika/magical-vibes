package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CommonBond;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.SmugglersCopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlasterCombatDJ.class, BlasterMoraleBooster.class, Battlegrowth.class, CommonBond.class,
        DoomBlade.class, FountainOfYouth.class, Ornithopter.class, Shatter.class, SmugglersCopter.class})
class BlasterCombatDJTest extends BaseCardTest {

    @Test
    void moreThanMeetsTheEyeCastsBlasterMoraleBoosterWithThreeCounters() {
        Permanent blaster = castConvertedBlaster();

        assertThat(blaster.isTransformed()).isTrue();
        assertThat(blaster.getCard()).isInstanceOf(BlasterMoraleBooster.class);
        assertThat(blaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void otherArtifactCreaturesAndVehiclesEnterWithOneCounter() {
        Permanent blaster = harness.addToBattlefieldAndReturn(player1, new BlasterCombatDJ());
        Permanent ornithopter = harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        Permanent copter = harness.enterBattlefieldAndReturn(player1, new SmugglersCopter());

        assertThat(blaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ornithopter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(copter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void activatedAbilityMovesCountersToAnotherArtifactAndConvertsAtZero() {
        Permanent blaster = castConvertedBlaster();
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, 3, fountain.getId());
        harness.passBothPriorities();

        assertThat(blaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(fountain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, fountain, Keyword.HASTE)).isTrue();
        assertThat(blaster.isTransformed()).isFalse();
        assertThat(blaster.getCard()).isInstanceOf(BlasterCombatDJ.class);
    }

    @Test
    void puttingCountersOnBlasterConvertsIt() {
        Permanent blaster = harness.addToBattlefieldAndReturn(player1, new BlasterCombatDJ());
        harness.setHand(player1, List.of(new CommonBond()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of(blaster.getId()));
        harness.passBothPriorities();

        assertThat(blaster.isTransformed()).isTrue();
        assertThat(blaster.getCard()).isInstanceOf(BlasterMoraleBooster.class);
    }

    @Test
    void modularDeathMayPutCountersOnAnArtifactCreature() {
        harness.addToBattlefield(player1, new BlasterCombatDJ());
        Permanent modular = addCreatureReady(player1, new Ornithopter());
        modular.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent recipient = addCreatureReady(player1, new Ornithopter());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, modular.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(recipient.getId());

        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void opponentPuttingCountersOnBlasterDoesNotConvertIt() {
        Permanent blaster = harness.addToBattlefieldAndReturn(player1, new BlasterCombatDJ());
        harness.setHand(player2, List.of(new Battlegrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, blaster.getId());

        assertThat(blaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(blaster.isTransformed()).isFalse();
    }

    @Test
    void pendingConversionTriggerDoesNotConvertBlasterAgain() {
        Permanent blaster = harness.addToBattlefieldAndReturn(player1, new BlasterCombatDJ());
        harness.setHand(player1, List.of(new Battlegrowth(), new Battlegrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, blaster.getId());
        harness.castAndResolveInstant(player1, 0, blaster.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(blaster.isTransformed()).isTrue();
        assertThat(blaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void movingMoreCountersThanAvailableMovesAllOfThem() {
        Permanent blaster = castConvertedBlaster();
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, 5, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, recipient, Keyword.HASTE)).isTrue();
        assertThat(blaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(blaster.isTransformed()).isFalse();
    }

    @Test
    void movingZeroCountersStillGrantsHasteWithoutConverting() {
        Permanent blaster = castConvertedBlaster();
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        harness.activateAbility(player1, 0, 0, 0, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, recipient, Keyword.HASTE)).isTrue();
        assertThat(blaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(blaster.isTransformed()).isTrue();
    }

    @Test
    void modularGrantDoesNotGiveCountersToOpponentsOrNoncreatureArtifacts() {
        harness.addToBattlefield(player1, new BlasterCombatDJ());
        Permanent opposingCreature = harness.enterBattlefieldAndReturn(player2, new Ornithopter());
        Permanent fountain = harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(fountain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void moraleBoosterModularCanBeDeclinedAndTargetsOnlyArtifactCreatures() {
        Permanent blaster = castConvertedBlaster();
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, blaster.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(recipient.getId()).doesNotContain(fountain.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Blaster, Combat DJ");
    }

    @Test
    void moraleBoosterModularPutsAllThreeCountersOnArtifactCreature() {
        Permanent blaster = castConvertedBlaster();
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, blaster.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void partialMoveLeavesBlasterConvertedAndHasteExpiresAtCleanup() {
        Permanent blaster = castConvertedBlaster();
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 1, recipient.getId());
        harness.passBothPriorities();

        assertThat(blaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(blaster.isTransformed()).isTrue();
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, recipient, Keyword.HASTE)).isTrue();
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(gqs.hasKeyword(gd, recipient, Keyword.HASTE)).isFalse();
    }

    @Test
    void moveAbilityCannotTargetBlasterItselfOrActivateOutsideMainPhase() {
        Permanent blaster = castConvertedBlaster();
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, blaster.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, recipient.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castConvertedBlaster() {
        harness.setHand(player1, List.of(new BlasterCombatDJ()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Blaster, Morale Booster");
    }
}
