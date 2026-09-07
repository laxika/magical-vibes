package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BuildersTalent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaretakersTalent.class, BuildersTalent.class, GrizzlyBears.class})
class CaretakersTalentTest extends BaseCardTest {

    @Test
    @DisplayName("Draws only once each turn when tokens enter under your control")
    void drawsOncePerTurnForTokenEntries() {
        castCaretakersTalent();
        harness.setHand(player1, List.of(new BuildersTalent(), new BuildersTalent()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handSizeBeforeFirstBuilder = gd.playerHands.get(player1.getId()).size();
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeFirstBuilder);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeFirstBuilder - 1);
    }

    @Test
    @DisplayName("At level 2, creates a copy of a target token you control")
    void levelTwoCopiesTargetToken() {
        Permanent caretaker = castCaretakersTalent();
        Permanent wall = createWall();
        int tokenCountBefore = countTokens(player1);

        levelUp(player1, caretaker, 0);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(wall.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SelfTriggeredAbilityTarget.class);

        harness.handlePermanentChosen(player1, wall.getId());
        resolveAllTriggers();

        assertThat(countTokens(player1)).isEqualTo(tokenCountBefore + 1);
    }

    @Test
    @DisplayName("At level 3, gives creature tokens you control +2/+2")
    void levelThreeBoostsCreatureTokensOnly() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent caretaker = castCaretakersTalent();
        Permanent wall = createWall();

        levelUp(player1, caretaker, 0);
        harness.handlePermanentChosen(player1, wall.getId());
        resolveAllTriggers();

        levelUp(player1, caretaker, 1);

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    private Permanent castCaretakersTalent() {
        harness.setHand(player1, List.of(new CaretakersTalent()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof CaretakersTalent)
                .findFirst()
                .orElseThrow();
    }

    private Permanent createWall() {
        harness.setHand(player1, List.of(new BuildersTalent()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }

    private void levelUp(Player player, Permanent caretaker, int abilityIndex) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.WHITE, 1);
        if (abilityIndex == 1) {
            harness.addMana(player, ManaColor.COLORLESS, 3);
        }

        int permanentIndex = gd.playerBattlefields.get(player.getId()).indexOf(caretaker);
        harness.activateAbility(player, permanentIndex, abilityIndex, null, null);
        harness.passBothPriorities();
    }

    private int countTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }
}
