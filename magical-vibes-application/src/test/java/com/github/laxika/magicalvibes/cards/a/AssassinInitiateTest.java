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

    @Test
    @DisplayName("One activation grants only the chosen keyword to its source")
    void grantsOnlyChosenKeywordToSource() {
        Permanent initiate = addInitiateReady();
        Permanent other = addCreatureReady(player1, new AssassinInitiate());
        Permanent opposing = addCreatureReady(player2, new AssassinInitiate());

        activateAndChoose("FLYING");

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, initiate, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Repeated activations can grant all three keywords simultaneously")
    void repeatedActivationsAccumulateKeywords() {
        Permanent initiate = addInitiateReady();

        activateAndChoose("FLYING");
        activateAndChoose("DEATHTOUCH");
        activateAndChoose("LIFELINK");

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, initiate, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, initiate, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A tapped summoning-sick Initiate can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new AssassinInitiate());
        initiate.setSummoningSick(true);
        initiate.setTapped(true);

        activateAndChoose("LIFELINK");

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isTrue();
        assertThat(initiate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An ability whose source left does not grant a keyword to another Initiate")
    void sourceLeavingBeforeResolutionDoesNotAffectAnotherInitiate() {
        Permanent initiate = addInitiateReady();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(initiate);
        gd.playerGraveyards.get(player1.getId()).add(initiate.getCard());
        Permanent other = addCreatureReady(player1, new AssassinInitiate());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.LIFELINK)).isFalse();
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
