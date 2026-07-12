package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SafeholdSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{W} and untapping gives +0/+2 until end of turn")
    void pumpsToughnessAndUntapsSource() {
        Permanent sentry = addTapped(player1, new SafeholdSentry());
        harness.addMana(player1, ManaColor.WHITE, 3);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sentry.getPowerModifier()).isEqualTo(0);
        assertThat(sentry.getToughnessModifier()).isEqualTo(2);
        // Paying {Q} untapped the source.
        assertThat(sentry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The +0/+2 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent sentry = addTapped(player1, new SafeholdSentry());
        harness.addMana(player1, ManaColor.WHITE, 3);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(sentry.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sentry.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addReady(player1, new SafeholdSentry());
        harness.addMana(player1, ManaColor.WHITE, 3);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTapped(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = addReady(player, card);
        perm.tap();
        return perm;
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
