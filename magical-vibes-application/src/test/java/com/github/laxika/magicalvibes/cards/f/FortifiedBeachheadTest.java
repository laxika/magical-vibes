package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.y.YotianFrontliner;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FortifiedBeachhead.class, ArgothianSprite.class, YotianFrontliner.class})
class FortifiedBeachheadTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no Soldier and cannot reveal one")
    void entersTappedWithoutSoldier() {
        playLand(new FortifiedBeachhead());

        assertThat(findPermanent(player1, "Fortified Beachhead").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Soldier")
    void entersUntappedWithControlledSoldier() {
        harness.addToBattlefield(player1, new YotianFrontliner());

        playLand(new FortifiedBeachhead());

        assertThat(findPermanent(player1, "Fortified Beachhead").isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Revealing a Soldier lets it enter untapped when you control no Soldier")
    void entersUntappedWhenRevealingSoldier() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FortifiedBeachhead(), new YotianFrontliner()));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Fortified Beachhead").isTapped()).isFalse();
        harness.assertInHand(player1, "Yotian Frontliner");
    }

    @Test
    @DisplayName("Declining to reveal a Soldier makes the land enter tapped")
    void entersTappedWhenRevealDeclined() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FortifiedBeachhead(), new YotianFrontliner()));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Fortified Beachhead").isTapped()).isTrue();
        harness.assertInHand(player1, "Yotian Frontliner");
    }

    @Test
    @DisplayName("An opponent's Soldier does not let the land enter untapped")
    void opponentSoldierDoesNotQualify() {
        harness.addToBattlefield(player2, new YotianFrontliner());

        playLand(new FortifiedBeachhead());

        assertThat(findPermanent(player1, "Fortified Beachhead").isTapped()).isTrue();
    }

    @Test
    @DisplayName("A non-Soldier in hand cannot be revealed for untapped entry")
    void nonSoldierInHandDoesNotQualify() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FortifiedBeachhead(), new ArgothianSprite()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Fortified Beachhead").isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A Soldier can still be revealed when you already control a Soldier")
    void canRevealDespiteControlledSoldier() {
        harness.addToBattlefield(player1, new YotianFrontliner());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FortifiedBeachhead(), new YotianFrontliner()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(findPermanent(player1, "Fortified Beachhead").isTapped()).isFalse();
        harness.assertInHand(player1, "Yotian Frontliner");
    }

    @Test
    @DisplayName("Tapping for white or blue mana produces the chosen color")
    void producesWhiteOrBlueMana() {
        addReadyLand();
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);

        addReadyLand();
        harness.activateAbility(player1, 1, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Soldier pump affects only Soldiers until end of turn")
    void pumpsSoldiersUntilEndOfTurn() {
        Permanent land = addReadyLand();
        Permanent soldier = addCreatureReady(player1, new YotianFrontliner());
        Permanent bear = addCreatureReady(player1, new ArgothianSprite());
        int soldierPower = gqs.getEffectivePower(gd, soldier);
        int soldierToughness = gqs.getEffectiveToughness(gd, soldier);
        int bearPower = gqs.getEffectivePower(gd, bear);
        int bearToughness = gqs.getEffectiveToughness(gd, bear);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(soldierPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(soldierToughness + 1);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(bearPower);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(bearToughness);
    }

    @Test
    @DisplayName("The pump expires at cleanup and excludes opponents and later arrivals")
    void pumpExpiresAndOnlyAffectsOwnSoldiersAtResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addReadyLand();
        Permanent soldier = addCreatureReady(player1, new YotianFrontliner());
        Permanent opponentSoldier = addCreatureReady(player2, new YotianFrontliner());
        int power = gqs.getEffectivePower(gd, soldier);
        int toughness = gqs.getEffectiveToughness(gd, soldier);
        int opponentPower = gqs.getEffectivePower(gd, opponentSoldier);
        int opponentToughness = gqs.getEffectiveToughness(gd, opponentSoldier);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(toughness + 1);
        assertThat(gqs.getEffectivePower(gd, opponentSoldier)).isEqualTo(opponentPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentSoldier)).isEqualTo(opponentToughness);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        Permanent laterSoldier = addCreatureReady(player1, new YotianFrontliner());
        assertThat(gqs.getEffectivePower(gd, laterSoldier)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, laterSoldier)).isEqualTo(toughness);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(toughness);
    }

    private void playLand(com.github.laxika.magicalvibes.model.Card land) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(land));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyLand() {
        return addCreatureReady(player1, new FortifiedBeachhead());
    }
}
