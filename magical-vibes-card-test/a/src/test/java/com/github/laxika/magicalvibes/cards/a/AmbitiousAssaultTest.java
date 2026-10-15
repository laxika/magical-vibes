package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ContainmentConstruct;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NinjasKunai;
import com.github.laxika.magicalvibes.cards.t.TamiyosCompleation;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmbitiousAssault.class, GrizzlyBears.class, ContainmentConstruct.class,
        NinjasKunai.class, TamiyosCompleation.class})
class AmbitiousAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures and draws if you control a modified creature")
    void boostsAndDrawsWithModifiedCreature() {
        Permanent modifiedBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent unmodifiedBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        modifiedBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new AmbitiousAssault()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, modifiedBear)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, unmodifiedBear)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw without a modified creature")
    void doesNotDrawWithoutModifiedCreature() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AmbitiousAssault()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The creature boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AmbitiousAssault()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void auraOnlyModifiesCreatureWhenControlledByItsController(boolean ownAura) {
        Permanent creature = addCreatureReady(player1, new ContainmentConstruct());
        Permanent aura = harness.addToBattlefieldAndReturn(
                ownAura ? player1 : player2, new TamiyosCompleation());
        aura.setAttachedTo(creature.getId());
        prepareAssault();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(ownAura ? 1 : 0);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void equipmentModifiesCreatureRegardlessOfEquipmentController(boolean ownEquipment) {
        Permanent creature = addCreatureReady(player1, new ContainmentConstruct());
        Permanent equipment = harness.addToBattlefieldAndReturn(
                ownEquipment ? player1 : player2, new NinjasKunai());
        equipment.setAttachedTo(creature.getId());
        prepareAssault();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @ParameterizedTest
    @EnumSource(value = CounterType.class, names = {"CHARGE", "STUN", "FLYING"})
    void countersNeedNotChangePowerOrToughnessToModifyCreature(CounterType counter) {
        Permanent creature = addCreatureReady(player1, new ContainmentConstruct());
        creature.setCounterCount(counter, 1);
        prepareAssault();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void modificationIsCheckedAtResolution(boolean modifiedWhenResolving) {
        Permanent creature = addCreatureReady(player1, new ContainmentConstruct());
        creature.setCounterCount(CounterType.CHARGE, modifiedWhenResolving ? 0 : 1);
        prepareAssault();
        harness.castInstant(player1, 0);

        creature.setCounterCount(CounterType.CHARGE, modifiedWhenResolving ? 1 : 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(modifiedWhenResolving ? 1 : 0);
    }

    @Test
    void opposingModifiedCreatureAndCounterOnNoncreatureDoNotAllowDraw() {
        Permanent ownCreature = addCreatureReady(player1, new ContainmentConstruct());
        Permanent opposingCreature = addCreatureReady(player2, new ContainmentConstruct());
        opposingCreature.setCounterCount(CounterType.CHARGE, 1);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new NinjasKunai());
        equipment.setCounterCount(CounterType.CHARGE, 1);
        prepareAssault();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void multipleModifiedCreaturesStillDrawOnlyOneCard() {
        addCreatureReady(player1, new ContainmentConstruct()).setCounterCount(CounterType.CHARGE, 1);
        addCreatureReady(player1, new ContainmentConstruct()).setCounterCount(CounterType.CHARGE, 1);
        prepareAssault();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void boostOnlyAppliesToCreaturesPresentWhenSpellResolves() {
        prepareAssault();
        harness.castInstant(player1, 0);
        Permanent beforeResolution = addCreatureReady(player1, new ContainmentConstruct());

        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new ContainmentConstruct());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void resolvesWithNoCreaturesWithoutDrawing() {
        prepareAssault();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Ambitious Assault");
    }

    private void prepareAssault() {
        harness.setHand(player1, List.of(new AmbitiousAssault()));
        harness.setLibrary(player1, List.of(
                new ContainmentConstruct(), new ContainmentConstruct(), new ContainmentConstruct()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
