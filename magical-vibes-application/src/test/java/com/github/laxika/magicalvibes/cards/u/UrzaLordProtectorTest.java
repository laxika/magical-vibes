package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.d.Defabricate;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RustGoliath;
import com.github.laxika.magicalvibes.cards.s.ShootDown;
import com.github.laxika.magicalvibes.cards.t.TheMightstoneAndWeakstone;
import com.github.laxika.magicalvibes.cards.t.TeferiTemporalPilgrim;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrzaLordProtector.class, UrzaPlaneswalker.class, TheMightstoneAndWeakstone.class,
        ArgothianSprite.class, EnergyRefractor.class, Forest.class, Mountain.class,
        TeferiTemporalPilgrim.class, RustGoliath.class, ShootDown.class, Defabricate.class})
class UrzaLordProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces artifact spells by {1}")
    void reducesArtifactSpells() {
        harness.addToBattlefield(player1, new UrzaLordProtector());
        harness.setHand(player1, List.of(new EnergyRefractor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not reduce creature spells")
    void doesNotReduceCreatureSpells() {
        harness.addToBattlefield(player1, new UrzaLordProtector());
        harness.setHand(player1, List.of(new ArgothianSprite()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Melds with The Mightstone and Weakstone")
    void meldsWithMightstoneAndWeakstone() {
        addCreatureReady(player1, new UrzaLordProtector());
        harness.addToBattlefield(player1, new TheMightstoneAndWeakstone());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        prepareMainPhase();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Urza, Lord Protector");
        harness.assertNotOnBattlefield(player1, "The Mightstone and Weakstone");
        harness.assertOnBattlefield(player1, "Urza, Planeswalker");
        assertThat(findPermanent(player1, "Urza, Planeswalker").getMeldComponentCards())
                .hasSize(2);
    }

    @Test
    @DisplayName("Urza's plus-two ability reduces matching spells and gains life")
    void plusTwoReducesMatchingSpellsAndGainsLife() {
        addReadyUrza(7);
        harness.setHand(player1, List.of(new EnergyRefractor()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Urza's plus-one ability draws two cards, then discards one")
    void plusOneDrawsAndDiscards() {
        addReadyUrza(7);
        harness.setHand(player1, List.of(new ArgothianSprite()));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Urza's zero ability creates two artifact Soldiers")
    void zeroCreatesArtifactSoldiers() {
        addReadyUrza(7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
        assertThat(findPermanents(player1, "Soldier"))
                .allMatch(permanent -> permanent.getCard().hasType(CardType.ARTIFACT));
    }

    @Test
    @DisplayName("Urza's minus-three ability exiles a nonland permanent")
    void minusThreeExilesNonlandPermanent() {
        addReadyUrza(7);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        UUID bearsId = bears.getId();

        harness.activateAbility(player1, 0, 3, null, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Argothian Sprite");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof ArgothianSprite);
    }

    @Test
    @DisplayName("Urza's minus-ten ability protects artifacts and planeswalkers")
    void minusTenProtectsArtifactsAndPlaneswalkers() {
        addReadyUrza(10);
        Permanent garruk = harness.addToBattlefieldAndReturn(player1, new TeferiTemporalPilgrim());
        garruk.setCounterCount(CounterType.LOYALTY, 5);
        harness.addToBattlefield(player1, new EnergyRefractor());
        harness.addToBattlefield(player1, new RustGoliath());
        harness.addToBattlefield(player1, new ArgothianSprite());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 4, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Urza, Planeswalker");
        harness.assertOnBattlefield(player1, "Teferi, Temporal Pilgrim");
        harness.assertOnBattlefield(player1, "Energy Refractor");
        harness.assertOnBattlefield(player1, "Rust Goliath");
        harness.assertNotOnBattlefield(player1, "Argothian Sprite");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Urza can activate loyalty abilities twice each turn")
    void canActivateLoyaltyAbilitiesTwice() {
        Permanent urza = addReadyUrza(7);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(urza.getCounterCount(CounterType.LOYALTY)).isEqualTo(11);
    }

    private Permanent addReadyUrza(int loyalty) {
        Permanent urza = harness.addToBattlefieldAndReturn(player1, new UrzaPlaneswalker());
        urza.setCounterCount(CounterType.LOYALTY, loyalty);
        urza.setSummoningSick(false);
        prepareMainPhase();
        return urza;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void meldAbilityWorksWhileUrzaIsTappedAndSummoningSick() {
        Permanent urza = harness.addToBattlefieldAndReturn(player1, new UrzaLordProtector());
        urza.tap();
        harness.addToBattlefield(player1, new TheMightstoneAndWeakstone());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        prepareMainPhase();

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Urza, Planeswalker");
    }

    @Test
    void meldAbilityRejectsActivationDuringUpkeep() {
        addCreatureReady(player1, new UrzaLordProtector());
        harness.addToBattlefield(player1, new TheMightstoneAndWeakstone());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tokenCopyOfMightstoneCannotMeld() {
        addCreatureReady(player1, new UrzaLordProtector());
        TheMightstoneAndWeakstone token = new TheMightstoneAndWeakstone();
        token.setToken(true);
        harness.addToBattlefield(player1, token);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        prepareMainPhase();

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Urza, Planeswalker");
        harness.assertNotOnBattlefield(player1, "Urza, Lord Protector");
        harness.assertNotOnBattlefield(player1, "The Mightstone and Weakstone");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof UrzaLordProtector);
    }

    @Test
    void cannotActivateThirdLoyaltyAbilityInSameTurn() {
        addReadyUrza(7);
        harness.activateAbility(player1, 0, 2, null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, 0, 2, null, null);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusThreeCannotTargetLand() {
        addReadyUrza(7);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusTenDoesNotProtectOpponentsArtifacts() {
        addReadyUrza(11);
        harness.addToBattlefield(player2, new EnergyRefractor());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, 4, null, null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Urza, Planeswalker");
        harness.assertNotOnBattlefield(player2, "Energy Refractor");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void frontFaceReducesSorceryCost() {
        harness.addToBattlefield(player1, new UrzaLordProtector());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EnergyRefractor());
        harness.setHand(player1, List.of(new ShootDown()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        prepareMainPhase();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card instanceof EnergyRefractor);
    }

    @Test
    void plusTwoReducesSorceryCost() {
        addReadyUrza(7);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EnergyRefractor());
        harness.setHand(player1, List.of(new ShootDown()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card instanceof EnergyRefractor);
    }

    @Test
    void urzaDoesNotGrantExtraLoyaltyActivationToOtherPlaneswalkers() {
        addReadyUrza(7);
        harness.addToBattlefield(player1, new TeferiTemporalPilgrim());
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));

        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void frontFaceReducesInstantCostWithoutReducingColoredMana() {
        harness.addToBattlefield(player1, new UrzaLordProtector());
        EnergyRefractor artifact = new EnergyRefractor();
        harness.setHand(player1, List.of(artifact, new Defabricate()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        prepareMainPhase();
        harness.castArtifact(player1, 0);

        harness.castInstant(player1, 0, 0, artifact.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifact);
    }

    @Test
    void plusTwoReducesInstantCostWithoutReducingColoredMana() {
        addReadyUrza(7);
        EnergyRefractor artifact = new EnergyRefractor();
        harness.setHand(player1, List.of(artifact, new Defabricate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();
        harness.castArtifact(player1, 0);

        harness.castInstant(player1, 0, 0, artifact.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifact);
    }

    @Test
    void meldAbilityWithoutPartnerStillPaysManaAndDoesNothing() {
        addCreatureReady(player1, new UrzaLordProtector());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        prepareMainPhase();

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Urza, Lord Protector");
        harness.assertNotOnBattlefield(player1, "Urza, Planeswalker");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void meldAbilityCannotUseAnArtifactOwnedByOpponent() {
        addCreatureReady(player1, new UrzaLordProtector());
        TheMightstoneAndWeakstone stolenArtifact = new TheMightstoneAndWeakstone();
        stolenArtifact.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, stolenArtifact);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        prepareMainPhase();

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Urza, Lord Protector");
        harness.assertOnBattlefield(player1, "The Mightstone and Weakstone");
        harness.assertNotOnBattlefield(player1, "Urza, Planeswalker");
    }

    @Test
    void frontFaceDoesNotReduceOpponentsArtifactSpells() {
        harness.addToBattlefield(player2, new UrzaLordProtector());
        harness.setHand(player1, List.of(new EnergyRefractor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void frontFaceCannotPayColoredManaWithItsReduction() {
        harness.addToBattlefield(player1, new UrzaLordProtector());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EnergyRefractor());
        harness.setHand(player1, List.of(new ShootDown()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castAndResolveSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void twoPlusTwoActivationsStackTheirCostReductions() {
        addReadyUrza(7);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EnergyRefractor());
        harness.setHand(player1, List.of(new ShootDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertLife(player1, 24);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card instanceof EnergyRefractor);
    }

    @Test
    void plusTwoDoesNotReduceNonartifactCreatureCost() {
        addReadyUrza(7);
        harness.setHand(player1, List.of(new ArgothianSprite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void plusTwoCostReductionExpiresAtEndOfTurn() {
        addReadyUrza(7);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        prepareMainPhase();
        harness.setHand(player1, List.of(new EnergyRefractor()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusTenIndestructibilityExpiresAtEndOfTurn() {
        addReadyUrza(11);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.activateAbility(player1, 0, 4, null, null);
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INDESTRUCTIBLE))
                .isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INDESTRUCTIBLE))
                .isFalse();
    }

    @Test
    void meldedUrzaMovesBothComponentsToGraveyardAtZeroLoyalty() {
        addCreatureReady(player1, new UrzaLordProtector());
        harness.addToBattlefield(player1, new TheMightstoneAndWeakstone());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        prepareMainPhase();
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();
        findPermanent(player1, "Urza, Planeswalker").setCounterCount(CounterType.LOYALTY, 0);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Urza, Planeswalker");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof UrzaLordProtector)
                .anyMatch(card -> card instanceof TheMightstoneAndWeakstone)
                .noneMatch(card -> card instanceof UrzaPlaneswalker);
    }
}
