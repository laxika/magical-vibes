package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XathridSlybladeTest extends BaseCardTest {

    @Test
    @DisplayName("Activating loses hexproof and gains first strike and deathtouch")
    void activationSwapsKeywords() {
        Permanent slyblade = addCreatureReady(player1, new XathridSlyblade());
        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.HEXPROOF)).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("The keyword changes wear off at end of turn")
    void keywordChangesWearOff() {
        Permanent slyblade = addCreatureReady(player1, new XathridSlyblade());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.DEATHTOUCH)).isFalse();
    }
}
