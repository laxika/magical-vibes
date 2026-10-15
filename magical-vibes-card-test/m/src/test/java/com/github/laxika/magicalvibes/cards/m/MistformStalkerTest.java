package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MistformStalker.class)
class MistformStalkerTest extends BaseCardTest {

    @Test
    void chosenCreatureTypeReplacesOldTypeUntilEndOfTurn() {
        Permanent stalker = addCreatureReady(player1, new MistformStalker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, stalker)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    void chosenCreatureTypeWearsOffAtEndOfTurn() {
        Permanent stalker = addCreatureReady(player1, new MistformStalker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.effectiveCreatureSubtypes(gd, stalker)).containsExactly(CardSubtype.ILLUSION);
    }

    @Test
    void secondAbilityBoostsAndGrantsFlying() {
        Permanent stalker = addCreatureReady(player1, new MistformStalker());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.FLYING)).isTrue();
    }

    @Test
    void secondAbilityWearsOffAtEndOfTurn() {
        Permanent stalker = addCreatureReady(player1, new MistformStalker());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.FLYING)).isFalse();
    }

    @Test
    void laterTypeChoiceReplacesEarlierChoiceWithoutRemovingBoostOrFlying() {
        Permanent stalker = addCreatureReady(player1, new MistformStalker());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.ELF.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, stalker)).containsExactly(CardSubtype.ELF);
        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.FLYING)).isTrue();
    }

    @Test
    void repeatedBoostsAccumulateAndExpireTogetherWithoutAffectingOtherCreatures() {
        Permanent stalker = addCreatureReady(player1, new MistformStalker());
        Permanent other = addCreatureReady(player1, new MistformStalker());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.FLYING)).isFalse();
    }
}
