package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SporeCrawler;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GloriousSunrise.class, Forest.class, DawnhartDisciple.class, SporeCrawler.class})
class GloriousSunriseTest extends BaseCardTest {

    private static final String PUMP_MODE =
            "Creatures you control get +1/+1 and gain trample until end of turn.";
    private static final String LAND_MODE =
            "Target land gains \"{T}: Add {G}{G}{G}\" until end of turn.";
    private static final String DRAW_MODE =
            "Draw a card if you control a creature with power 3 or greater.";
    private static final String LIFE_MODE = "You gain 3 life.";

    @Test
    void pumpModeBoostsOwnCreaturesAndGrantsTrampleUntilEndOfTurn() {
        addSunrise();
        Permanent ownCreature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent opposingCreature = addCreatureReady(player2, new DawnhartDisciple());

        choose(PUMP_MODE);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();

        declareAttackers(List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void landModeGrantsAThreeGreenManaAbilityUntilEndOfTurn() {
        addSunrise();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        choose(LAND_MODE);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        int forestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forest);
        harness.activateAbility(player1, forestIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    void drawModeDrawsOnlyWhenTheConditionIsTrueOnResolution() {
        harness.setLibrary(player1, List.of(new Forest()));
        addSunrise();
        addCreatureReady(player1, new SporeCrawler());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        choose(DRAW_MODE);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void drawModeCanBeChosenWithoutAQualifyingCreature() {
        harness.setLibrary(player1, List.of(new Forest()));
        addSunrise();
        addCreatureReady(player1, new DawnhartDisciple());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        choose(DRAW_MODE);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void lifeModeGainsThreeLife() {
        harness.setLife(player1, 10);
        addSunrise();

        choose(LIFE_MODE);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        addSunrise();
        harness.setLife(player1, 10);

        advanceToCombat(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 10);
    }

    @Test
    void pumpDoesNotAffectCreaturesEnteringAfterResolution() {
        addSunrise();
        Permanent existing = addCreatureReady(player1, new DawnhartDisciple());

        choose(PUMP_MODE);
        Permanent later = harness.enterBattlefieldAndReturn(player1, new SporeCrawler());

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, existing, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, later, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void drawModeIgnoresOpponentsQualifyingCreature() {
        harness.setLibrary(player1, List.of(new Forest()));
        addSunrise();
        addCreatureReady(player2, new SporeCrawler());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        choose(DRAW_MODE);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void drawModeUsesEffectivePowerAtResolution() {
        harness.setLibrary(player1, List.of(new Forest()));
        addSunrise();
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, () -> {
            advanceToCombat(player1);
            harness.handleListChoice(player1, DRAW_MODE);
            assertThat(gd.stack).hasSize(1);
            creature.setPowerModifier(1);
            harness.passBothPriorities();
        });

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void drawModeDoesNotDrawIfPowerFallsBelowThreeBeforeResolution() {
        harness.setLibrary(player1, List.of(new Forest()));
        addSunrise();
        Permanent creature = addCreatureReady(player1, new SporeCrawler());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, () -> {
            advanceToCombat(player1);
            harness.handleListChoice(player1, DRAW_MODE);
            assertThat(gd.stack).hasSize(1);
            creature.setPowerModifier(-1);
            harness.passBothPriorities();
        });

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void landModeCanGrantManaAbilityToOpponentsLand() {
        addSunrise();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        choose(LAND_MODE);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();
        int forestIndex = gd.playerBattlefields.get(player2.getId()).indexOf(forest);
        harness.activateAbility(player2, forestIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    void landModeExpiresAndRetainsTheOriginalManaAbility() {
        addSunrise();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        choose(LAND_MODE);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();
        declareAttackers(List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        int forestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forest);
        harness.tapPermanent(player1, forestIndex);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(forest.isTapped()).isTrue();
    }

    private void addSunrise() {
        harness.addToBattlefieldAndReturn(player1, new GloriousSunrise());
    }

    private void choose(String mode) {
        advanceToCombat(player1);
        harness.handleListChoice(player1, mode);
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
    }
}
