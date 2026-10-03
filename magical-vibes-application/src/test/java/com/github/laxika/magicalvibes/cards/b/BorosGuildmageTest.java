package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BorosGuildmage.class, LastGasp.class})
class BorosGuildmageTest extends BaseCardTest {

    @ParameterizedTest
    @CsvSource({"0, RED, HASTE", "1, WHITE, FIRST_STRIKE"})
    void tappedSummoningSickGuildmageCanTargetItself(int abilityIndex, ManaColor color, Keyword keyword) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BorosGuildmage());
        source.setSummoningSick(true);
        source.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, color, 1);

        harness.activateAbility(player1, 0, abilityIndex, null, source.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, keyword)).isTrue();
        assertThat(source.isTapped()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"0, WHITE", "1, RED"})
    void requiresTheCorrectColoredMana(int abilityIndex, ManaColor wrongColor) {
        Permanent source = addCreatureReady(player1, new BorosGuildmage());
        harness.addMana(player1, wrongColor, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, source, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"0, RED, HASTE", "1, WHITE, FIRST_STRIKE"})
    void abilityResolvesAfterSourceDies(int abilityIndex, ManaColor color, Keyword keyword) {
        Permanent source = addCreatureReady(player1, new BorosGuildmage());
        Permanent target = addCreatureReady(player2, new BorosGuildmage());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, color, 1);
        harness.activateAbility(player1, 0, abilityIndex, null, target.getId());

        harness.setHand(player1, List.of(new LastGasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.assertNotOnBattlefield(player1, "Boros Guildmage");
        harness.assertInGraveyard(player1, "Boros Guildmage");
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, target, keyword)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"0, RED", "1, WHITE"})
    void abilityDoesNotResolveAfterTargetDies(int abilityIndex, ManaColor color) {
        Permanent source = addCreatureReady(player1, new BorosGuildmage());
        Permanent target = addCreatureReady(player2, new BorosGuildmage());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, color, 1);
        harness.activateAbility(player1, 0, abilityIndex, null, target.getId());

        harness.setHand(player1, List.of(new LastGasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertNotOnBattlefield(player2, "Boros Guildmage");
        harness.assertInGraveyard(player2, "Boros Guildmage");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        assertThat(gqs.hasKeyword(gd, source, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("{1}{R}: target creature gains haste until end of turn")
    void grantsHaste() {
        addCreatureReady(player1, new BorosGuildmage());
        Permanent creature = addCreatureReady(player2, new BorosGuildmage());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Can target a creature you control")
    void grantsHasteToYourCreature() {
        Permanent source = addCreatureReady(player1, new BorosGuildmage());
        Permanent creature = addCreatureReady(player1, new BorosGuildmage());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, source, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("{1}{W}: target creature gains first strike until end of turn")
    void grantsFirstStrike() {
        addCreatureReady(player1, new BorosGuildmage());
        Permanent creature = addCreatureReady(player2, new BorosGuildmage());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void grantedKeywordsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new BorosGuildmage());
        Permanent creature = addCreatureReady(player2, new BorosGuildmage());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }
}
