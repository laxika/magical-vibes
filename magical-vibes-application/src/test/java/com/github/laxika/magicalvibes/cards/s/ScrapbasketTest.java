package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scrapbasket.class})
class ScrapbasketTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {1} makes it all five colors until end of turn")
    void activatingMakesItAllColors() {
        Permanent scrapbasket = harness.addToBattlefieldAndReturn(player1, new Scrapbasket());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Artifact creature starts colorless.
        assertThat(gqs.getEffectiveColors(gd, scrapbasket)).isEmpty();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, scrapbasket))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK,
                        CardColor.RED, CardColor.GREEN);
    }

    @Test
    @DisplayName("The all-colors setting wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent scrapbasket = harness.addToBattlefieldAndReturn(player1, new Scrapbasket());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveColors(gd, scrapbasket)).hasSize(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, scrapbasket)).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without paying the {1} cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefieldAndReturn(player1, new Scrapbasket());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Scrapbasket can activate and changes only when the ability resolves")
    void tappedNewCreatureCanActivateWithoutChangingOtherCreatures() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Scrapbasket());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new Scrapbasket());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Scrapbasket());
        source.setSummoningSick(true);
        source.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectiveColors(gd, source)).isEmpty();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, source))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK,
                        CardColor.RED, CardColor.GREEN);
        assertThat(source.isTapped()).isTrue();
        assertThat(gqs.getEffectiveColors(gd, other)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, opponent)).isEmpty();
    }

    @Test
    @DisplayName("Repeated activations are allowed and all expire at cleanup")
    void repeatedActivationsExpireTogether() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Scrapbasket());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, source)).hasSize(5);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, source)).isEmpty();
    }
}
