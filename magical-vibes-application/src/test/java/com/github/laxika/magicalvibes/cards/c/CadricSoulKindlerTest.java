package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlackbladeReforged;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.t.TsaboTavoc;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CadricSoulKindler.class, TsaboTavoc.class, BlackbladeReforged.class, SolRing.class})
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
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new TsaboTavoc(), "{5}{B}{R}");
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

    @Test
    void choosingNontokenLegendKeepsExemptTokens() {
        harness.addToBattlefield(player1, new CadricSoulKindler());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TsaboTavoc());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TsaboTavoc());
        Permanent token = addToken(player1, new TsaboTavoc());

        harness.runStateBasedActions();
        harness.handlePermanentChosen(player1, first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, token).doesNotContain(second);
    }

    @Test
    void copiesEnteringLegendThatDiedToLegendRule() {
        harness.addToBattlefield(player1, new CadricSoulKindler());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TsaboTavoc());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new TsaboTavoc(), "{5}{B}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void mayDeclineCopy() {
        harness.addToBattlefield(player1, new CadricSoulKindler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new TsaboTavoc(), "{5}{B}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    void enteringCadricDoesNotTriggerItself() {
        harness.castFromHand(player1, new CadricSoulKindler(), "{2}{R}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opposingLegendDoesNotTrigger() {
        harness.addToBattlefield(player1, new CadricSoulKindler());
        harness.enterBattlefieldAndReturn(player2, new TsaboTavoc());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void tokenEnteringDoesNotTriggerCopy() {
        harness.addToBattlefield(player1, new CadricSoulKindler());
        Card tokenCard = new TsaboTavoc();
        tokenCard.setToken(true);
        harness.enterBattlefieldAndReturn(player1, tokenCard);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void copiesLegendaryNoncreaturePermanent() {
        harness.addToBattlefield(player1, new CadricSoulKindler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new BlackbladeReforged(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void nonlegendaryPermanentDoesNotTriggerCopy() {
        harness.addToBattlefield(player1, new CadricSoulKindler());
        harness.castFromHand(player1, new SolRing(), "{1}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void sacrificeWaitsForDelayedTriggerToResolve() {
        Permanent token = createCopy();

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    void tokenControlledByOpponentIsNotSacrificed() {
        Permanent token = createCopy();
        gd.playerBattlefields.get(player1.getId()).remove(token);
        gd.playerBattlefields.get(player2.getId()).add(token);

        harness.passUntil(TurnStep.END_STEP);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);
    }

    private Permanent createCopy() {
        harness.addToBattlefield(player1, new CadricSoulKindler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new TsaboTavoc(), "{5}{B}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
    }

    private Permanent addToken(Player player, Card card) {
        card.setToken(true);
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
