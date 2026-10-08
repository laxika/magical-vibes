package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisageBandit.class, GrizzlyBears.class})
class VisageBanditTest extends BaseCardTest {

    @Test
    @DisplayName("May copy a creature you control and keeps the added Shapeshifter and Rogue types")
    void copiesCreatureYouControlWithAdditionalSubtypes() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        VisageBandit bandit = new VisageBandit();
        harness.castFromHand(player1, bandit, "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent copied = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(bandit.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, copied)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copied)).isEqualTo(2);
        assertThat(copied.getCard().getSubtypes())
                .contains(CardSubtype.BEAR, CardSubtype.SHAPESHIFTER, CardSubtype.ROGUE);
    }

    @Test
    @DisplayName("May decline copying")
    void mayDeclineCopy() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        VisageBandit bandit = new VisageBandit();
        harness.castFromHand(player1, bandit, "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(bandit.getId()))
                .findFirst().orElseThrow();
        assertThat(entered.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.SHAPESHIFTER, CardSubtype.ROGUE);
    }

    @Test
    void cannotChooseOpponentsCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new VisageBandit());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new VisageBandit());
        harness.castFromHand(player1, new VisageBandit(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, own.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    void entersWithoutCopyWhenOnlyOpponentControlsCreatures() {
        harness.addToBattlefield(player2, new VisageBandit());
        harness.castFromHand(player1, new VisageBandit(), "{3}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Visage Bandit");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void copyingDoesNotCopyCountersOrTappedState() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new VisageBandit());
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        own.tap();
        VisageBandit bandit = new VisageBandit();
        harness.castFromHand(player1, bandit, "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, own.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(bandit.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.getPlusOnePlusOneCounters()).isZero();
        assertThat(copy.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(5);
    }

    @Test
    void plotsForThreeManaAndCopiesAfterFreeCastOnLaterTurn() {
        VisageBandit bandit = new VisageBandit();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(bandit));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.plottedCardIds).contains(bandit.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId()).contains(bandit.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThatThrownBy(() -> harness.castFromExile(player1, bandit.getId()))
                .hasMessageContaining("on the turn it became plotted");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, bandit.getId()))
                .hasMessageContaining("sorcery speed");
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        Permanent own = harness.addToBattlefieldAndReturn(player1, new VisageBandit());
        harness.castFromExile(player1, bandit.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, own.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }
}
