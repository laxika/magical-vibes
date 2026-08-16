package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArgivianAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives -1/-1 and grants chosen flying until end of turn")
    void resolvingGrantsMinusAndFlying() {
        Permanent avenger = addAvengerReady();
        int power = gqs.getEffectivePower(gd, avenger);
        int toughness = gqs.getEffectiveToughness(gd, avenger);

        activateAvenger();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FLYING");

        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(power - 1);
        assertThat(gqs.getEffectiveToughness(gd, avenger)).isEqualTo(toughness - 1);
        assertThat(gqs.hasKeyword(gd, avenger, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can choose vigilance, deathtouch, or haste")
    void canChooseOtherKeywords() {
        assertChosenKeyword(Keyword.VIGILANCE);
        assertChosenKeyword(Keyword.DEATHTOUCH);
        assertChosenKeyword(Keyword.HASTE);
    }

    @Test
    @DisplayName("The -1/-1 and granted keyword wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent avenger = addAvengerReady();
        int power = gqs.getEffectivePower(gd, avenger);

        activateAvenger();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "HASTE");

        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(power - 1);
        assertThat(gqs.hasKeyword(gd, avenger, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(power);
        assertThat(gqs.hasKeyword(gd, avenger, Keyword.HASTE)).isFalse();
    }

    private void assertChosenKeyword(Keyword keyword) {
        Permanent avenger = addAvengerReady();
        int permanentIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;

        activateAvenger(permanentIndex);
        harness.passBothPriorities();
        harness.handleListChoice(player1, keyword.name());

        assertThat(gqs.hasKeyword(gd, avenger, keyword)).isTrue();
    }

    private Permanent addAvengerReady() {
        return addCreatureReady(player1, new ArgivianAvenger());
    }

    private void activateAvenger() {
        activateAvenger(0);
    }

    private void activateAvenger(int permanentIndex) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, permanentIndex, 0, null, null);
    }
}
