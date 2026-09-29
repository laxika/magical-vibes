package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.TsaboTavoc;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CadricSoulKindler.class, TsaboTavoc.class})
class CadricSoulKindlerTest extends BaseCardTest {

    @Test
    @DisplayName("Legendary tokens you control are exempt from the legend rule")
    void legendaryTokensAreExemptFromLegendRule() {
        harness.addToBattlefield(player1, new CadricSoulKindler());
        addToken(player1, new TsaboTavoc());
        addToken(player1, new TsaboTavoc());

        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The legend rule still applies to duplicate nontoken legendary permanents")
    void nontokenLegendariesAreNotExempt() {
        harness.addToBattlefield(player1, new CadricSoulKindler());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TsaboTavoc());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TsaboTavoc());

        harness.runStateBasedActions();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Paying creates a hasty legendary token copy scheduled for sacrifice")
    void payingCreatesHastyTokenCopySacrificedAtEndStep() {
        harness.addToBattlefield(player1, new CadricSoulKindler());
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player1, List.of(new TsaboTavoc()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
    }

    private Permanent addToken(Player player, Card card) {
        card.setToken(true);
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
