package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FangbladeBrigand.class, FangbladeEviscerator.class, DawnhartRejuvenator.class})
class FangbladeBrigandTest extends BaseCardTest {

    @Test
    @DisplayName("The front face gets +1/+0 and first strike until end of turn")
    void frontFaceAbilityBoostsAndGrantsFirstStrike() {
        Permanent brigand = addReadyBrigand();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(brigand.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, brigand, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(brigand.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, brigand, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The back face pumps all creatures its controller controls")
    void backFaceAbilityBoostsOwnCreaturesOnly() {
        Permanent brigand = addReadyBrigand();
        transformToBack(brigand);
        Permanent ownCreature = addCreatureReady(player1, new DawnhartRejuvenator());
        Permanent opposingCreature = addCreatureReady(player2, new DawnhartRejuvenator());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(brigand.getPowerModifier()).isEqualTo(2);
        assertThat(ownCreature.getPowerModifier()).isEqualTo(2);
        assertThat(opposingCreature.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Day and night transform the two faces")
    void dayAndNightTransformTheFaces() {
        gd.dayNight = DayNight.DAY;
        Permanent brigand = addReadyBrigand();

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);
        assertThat(brigand.getCard()).isInstanceOf(FangbladeEviscerator.class);
        assertThat(brigand.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player2.getId(), 2);
        harness.performUntapStep(player2);
        assertThat(brigand.getCard()).isInstanceOf(FangbladeBrigand.class);
        assertThat(brigand.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("The back face can activate its self boost repeatedly while tapped")
    void backFaceSelfBoostStacksAndExpires() {
        Permanent brigand = addReadyBrigand();
        transformToBack(brigand);
        brigand.tap();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(brigand.getPowerModifier()).isEqualTo(2);
        assertThat(brigand.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, brigand, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(brigand.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, brigand, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The team boost includes creatures present at resolution and excludes later arrivals")
    void teamBoostUsesCreaturesAtResolutionAndExpires() {
        Permanent brigand = addReadyBrigand();
        transformToBack(brigand);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        Permanent beforeResolution = addCreatureReady(player1, new DawnhartRejuvenator());
        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new DawnhartRejuvenator());

        assertThat(brigand.getPowerModifier()).isEqualTo(2);
        assertThat(beforeResolution.getPowerModifier()).isEqualTo(2);
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(beforeResolution.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.FIRST_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(brigand.getPowerModifier()).isZero();
        assertThat(beforeResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Entering while neither day nor night makes it day")
    void enteringEstablishesDay() {
        gd.dayNight = DayNight.NEITHER;

        Permanent brigand = harness.enterBattlefieldAndReturn(player1, new FangbladeBrigand());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(brigand.getCard()).isInstanceOf(FangbladeBrigand.class);
        assertThat(brigand.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Entering at night puts the back face onto the battlefield")
    void enteringAtNightUsesBackFace() {
        gd.dayNight = DayNight.NIGHT;

        Permanent brigand = harness.enterBattlefieldAndReturn(player1, new FangbladeBrigand());

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(brigand.getCard()).isInstanceOf(FangbladeEviscerator.class);
        assertThat(brigand.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Spells cast by the nonactive player do not keep it day")
    void onlyPreviousActivePlayersSpellsCountForDay() {
        gd.dayNight = DayNight.DAY;
        Permanent brigand = addReadyBrigand();
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(brigand.getCard()).isInstanceOf(FangbladeEviscerator.class);
    }

    @Test
    @DisplayName("One spell by the active player keeps it night despite the opponent casting two")
    void oneSpellKeepsItNight() {
        gd.dayNight = DayNight.NIGHT;
        Permanent brigand = addReadyBrigand();
        transformToBack(brigand);
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(brigand.getCard()).isInstanceOf(FangbladeEviscerator.class);
    }

    private Permanent addReadyBrigand() {
        return addCreatureReady(player1, new FangbladeBrigand());
    }

    private void transformToBack(Permanent brigand) {
        brigand.setCard(brigand.getOriginalCard().getBackFaceCard());
        brigand.setTransformed(true);
    }
}
