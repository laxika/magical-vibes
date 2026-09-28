package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BorosGuildmage.class)
class BorosGuildmageTest extends BaseCardTest {

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
