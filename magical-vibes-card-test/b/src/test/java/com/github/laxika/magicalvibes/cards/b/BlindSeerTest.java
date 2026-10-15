package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.PincerSpider;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlindSeer.class, PincerSpider.class})
class BlindSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Target permanent becomes the chosen color until end of turn")
    void targetPermanentBecomesChosenColorUntilEndOfTurn() {
        harness.addToBattlefield(player1, new BlindSeer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PincerSpider());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Target spell becomes the chosen color and carries it onto a permanent until end of turn")
    void targetSpellBecomesChosenColorUntilEndOfTurn() {
        harness.addToBattlefield(player1, new BlindSeer());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new PincerSpider(), "{2}{G}");
        harness.passPriority(player2);

        UUID spellId = gd.stack.getFirst().getCard().getId();
        harness.activateAbility(player1, 0, 0, null, spellId, Zone.STACK);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");
        assertThat(gqs.getEffectiveCardColors(gd, gd.stack.getFirst().getCard()))
                .containsExactly(CardColor.RED);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Pincer Spider");
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }

    @ParameterizedTest
    @EnumSource(CardColor.class)
    @DisplayName("Blind Seer can target itself and choose any of the five colors")
    void canTargetItselfAndChooseAnyColor(CardColor color) {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new BlindSeer());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, seer.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, color.name());

        assertThat(gqs.getEffectiveColors(gd, seer)).containsExactly(color);
        assertThat(seer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Repeated activations replace the previous color without tapping Blind Seer")
    void repeatedActivationsReplacePreviousColor() {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new BlindSeer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PincerSpider());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);
        assertThat(seer.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }
}
