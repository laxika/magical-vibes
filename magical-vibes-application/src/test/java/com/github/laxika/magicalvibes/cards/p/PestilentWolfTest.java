package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PestilentWolf.class)
class PestilentWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Activating grants deathtouch until end of turn")
    void activatingGrantsDeathtouch() {
        Permanent wolf = addCreatureReady(player1, new PestilentWolf());
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isFalse();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent wolf = addCreatureReady(player1, new PestilentWolf());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Wolf can activate without granting deathtouch to other Wolves")
    void tappedSummoningSickWolfGrantsOnlyItselfDeathtouch() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new PestilentWolf());
        wolf.setSummoningSick(true);
        wolf.setTapped(true);
        Permanent otherWolf = addCreatureReady(player1, new PestilentWolf());
        Permanent opposingWolf = addCreatureReady(player2, new PestilentWolf());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherWolf, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingWolf, Keyword.DEATHTOUCH)).isFalse();
        assertThat(wolf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activation requires the full three mana")
    void cannotActivateWithInsufficientMana() {
        Permanent wolf = addCreatureReady(player1, new PestilentWolf());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Generic mana cannot replace the required green mana")
    void cannotActivateWithoutGreenMana() {
        Permanent wolf = addCreatureReady(player1, new PestilentWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isFalse();
    }
}
