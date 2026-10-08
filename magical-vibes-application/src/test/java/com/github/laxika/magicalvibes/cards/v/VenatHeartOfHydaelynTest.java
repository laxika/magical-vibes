package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HydaelynTheMothercrystal;
import com.github.laxika.magicalvibes.cards.i.InBolassClutches;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VenatHeartOfHydaelyn.class, HydaelynTheMothercrystal.class, AdelizTheCinderWind.class,
        GrizzlyBears.class, Forest.class, InBolassClutches.class})
class VenatHeartOfHydaelynTest extends BaseCardTest {

    @Test
    @DisplayName("Draws only once each turn when a legendary spell is cast")
    void drawsOnlyOnceEachTurnForLegendarySpells() {
        harness.addToBattlefield(player1, new VenatHeartOfHydaelyn());
        harness.setHand(player1, List.of(new AdelizTheCinderWind(), new AdelizTheCinderWind()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Exiles a nonland permanent and transforms at sorcery speed")
    void exilesNonlandPermanentAndTransforms() {
        Permanent venat = addReadyVenat(player1);
        Permanent target = addReadyVenat(player2);
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, battlefieldIndex(player1, venat), 0, target.getId(), null);
        harness.passBothPriorities();

        assertThat(venat.isTransformed()).isTrue();
        assertThat(venat.getCard().getName()).isEqualTo("Hydaelyn, the Mothercrystal");
        harness.assertNotOnBattlefield(player2, "Venat, Heart of Hydaelyn");
    }

    @Test
    @DisplayName("Transform ability rejects lands")
    void transformAbilityRejectsLandTarget() {
        Permanent venat = addReadyVenat(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, venat), 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Blessing of Light strengthens and protects a legendary creature")
    void blessingOfLightCountersProtectsAndDrawsForLegendaryTarget() {
        Permanent hydaelyn = addTransformedHydaelyn(player1);
        Permanent target = addCreatureReady(player1, new AdelizTheCinderWind());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(hydaelyn.getCard().getName()).isEqualTo("Hydaelyn, the Mothercrystal");
    }

    @Test
    @DisplayName("Blessing of Light does not draw for a nonlegendary target")
    void blessingOfLightDoesNotDrawForNonlegendaryTarget() {
        addTransformedHydaelyn(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void nonlegendarySpellDoesNotConsumeTheLegendaryTrigger() {
        harness.addToBattlefield(player1, new VenatHeartOfHydaelyn());
        harness.setHand(player1, List.of(new GrizzlyBears(), new AdelizTheCinderWind()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    void opponentsLegendarySpellDoesNotTriggerDraw() {
        harness.addToBattlefield(player1, new VenatHeartOfHydaelyn());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new VenatHeartOfHydaelyn()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTransformWhenTheExileTargetLeavesBeforeResolution() {
        Permanent venat = addReadyVenat(player1);
        Permanent target = addReadyVenat(player2);
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.activateAbility(player1, battlefieldIndex(player1, venat), 0, target.getId(), null);
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(venat.isTransformed()).isFalse();
        assertThat(venat.isTapped()).isTrue();
    }

    @Test
    void transformAbilityRejectsActivationDuringCombat() {
        Permanent venat = addReadyVenat(player1);
        Permanent target = addReadyVenat(player2);
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, venat), 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void blessingOfLightRejectsSelfAndOpponentsCreatures() {
        Permanent hydaelyn = addTransformedHydaelyn(player1);
        Permanent target = addReadyVenat(player1);
        Permanent opponent = addReadyVenat(player2);
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, hydaelyn.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hydaelyn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void blessingOfLightDoesNotTriggerOnOpponentsTurn() {
        addTransformedHydaelyn(player1);
        Permanent target = addReadyVenat(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void blessingOfLightDoesNotDrawWhenItsTargetLeavesBeforeResolution() {
        addTransformedHydaelyn(player1);
        Permanent target = addReadyVenat(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void blessingProtectionLastsThroughOpponentsTurnAndExpiresOnYourNextTurn() {
        addTransformedHydaelyn(player1);
        Permanent target = addReadyVenat(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void blessingProtectionExpiresOnYourNextTurnEvenIfOpponentStealsTheCreature() {
        addTransformedHydaelyn(player1);
        Permanent target = addReadyVenat(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new InBolassClutches()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castEnchantment(player2, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyVenat(Player player) {
        return addCreatureReady(player, new VenatHeartOfHydaelyn());
    }

    private Permanent addTransformedHydaelyn(Player player) {
        VenatHeartOfHydaelyn front = new VenatHeartOfHydaelyn();
        Permanent permanent = harness.addToBattlefieldAndReturn(player, front);
        permanent.setCard(front.getBackFaceCard());
        permanent.setTransformed(true);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
