package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PitilessPontiffTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices another creature and gains deathtouch and indestructible")
    void sacrificesAnotherCreatureAndGainsKeywords() {
        Permanent pontiff = addReadyPontiff(player1);
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pontiff, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, pontiff, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Pitiless Pontiff");
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void grantedKeywordsWearOffAtEndOfTurn() {
        Permanent pontiff = addReadyPontiff(player1);
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pontiff, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, pontiff, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without another creature to sacrifice")
    void cannotActivateWithoutAnotherCreature() {
        addReadyPontiff(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPontiff(Player player) {
        Permanent pontiff = new Permanent(new PitilessPontiff());
        pontiff.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(pontiff);
        return pontiff;
    }
}
