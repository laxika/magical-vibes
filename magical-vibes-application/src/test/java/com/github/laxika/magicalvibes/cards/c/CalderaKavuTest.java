package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CalderaKavuTest extends BaseCardTest {

    @Test
    @DisplayName("The black ability gives Caldera Kavu +1/+1 until end of turn")
    void boostsSelf() {
        Permanent kavu = addReadyKavu();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kavu.getEffectivePower()).isEqualTo(3);
        assertThat(kavu.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The green ability prompts for a color without requiring a target")
    void promptsForColor() {
        addReadyKavu();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("The green ability changes Caldera Kavu's color until end of turn")
    void becomesChosenColorUntilEndOfTurn() {
        Permanent kavu = addReadyKavu();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectiveColors(gd, kavu)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Both temporary abilities wear off at end of turn")
    void temporaryAbilitiesWearOffAtEndOfTurn() {
        Permanent kavu = addReadyKavu();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        kavu.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(kavu.getEffectivePower()).isEqualTo(2);
        assertThat(kavu.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, kavu)).containsExactly(CardColor.RED);
    }

    private Permanent addReadyKavu() {
        Permanent kavu = new Permanent(new CalderaKavu());
        kavu.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kavu);
        return kavu;
    }
}
