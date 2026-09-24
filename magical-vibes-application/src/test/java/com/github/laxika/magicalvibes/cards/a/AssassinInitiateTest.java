package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AssassinInitiate.class)
class AssassinInitiateTest extends BaseCardTest {

    @Test
    @DisplayName("Can choose flying")
    void canChooseFlying() {
        Permanent initiate = addInitiateReady();

        activateAndChoose("FLYING");

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can choose deathtouch")
    void canChooseDeathtouch() {
        Permanent initiate = addInitiateReady();

        activateAndChoose("DEATHTOUCH");

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Can choose lifelink")
    void canChooseLifelink() {
        Permanent initiate = addInitiateReady();

        activateAndChoose("LIFELINK");

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Chosen keyword wears off at end of turn")
    void chosenKeywordWearsOffAtEndOfTurn() {
        Permanent initiate = addInitiateReady();

        activateAndChoose("FLYING");
        assertThat(gqs.hasKeyword(gd, initiate, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.FLYING)).isFalse();
    }

    private Permanent addInitiateReady() {
        return addCreatureReady(player1, new AssassinInitiate());
    }

    private void activateAndChoose(String keyword) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, keyword);
    }
}
