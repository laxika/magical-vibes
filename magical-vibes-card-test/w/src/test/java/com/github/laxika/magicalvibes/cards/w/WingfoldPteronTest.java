package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GustOfWind;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WingfoldPteron.class, GustOfWind.class})
class WingfoldPteronTest extends BaseCardTest {

    @Test
    void entersWithFlyingCounterWhenChosen() {
        Permanent pteron = castAndChoose("flying");

        assertThat(pteron.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(pteron.getCounterCount(CounterType.HEXPROOF)).isZero();
        assertThat(pteron.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(pteron.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void entersWithHexproofCounterWhenChosen() {
        Permanent pteron = castAndChoose("hexproof");

        assertThat(pteron.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(pteron.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
        assertThat(pteron.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(pteron.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void flyingCounterEnablesFlyingDependentCostReduction() {
        castAndChoose("flying");
        harness.addToBattlefield(player2, new WingfoldPteron());
        Permanent target = findPermanent(player2, "Wingfold Pteron");
        harness.setHand(player1, List.of(new GustOfWind()));
        harness.setLibrary(player1, List.of(new WingfoldPteron()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Wingfold Pteron");
        harness.assertInHand(player2, "Wingfold Pteron");
        harness.assertInGraveyard(player1, "Gust of Wind");
    }

    @Test
    void hexproofCounterPreventsOpponentsFromTargeting() {
        Permanent pteron = castAndChoose("hexproof");
        harness.setHand(player2, List.of(new GustOfWind()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, pteron.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");

        harness.assertOnBattlefield(player1, "Wingfold Pteron");
        harness.assertInHand(player2, "Gust of Wind");
    }

    @Test
    void returningAndRecastingAllowsNewChoiceWithoutOldCounter() {
        Permanent original = castAndChoose("flying");
        harness.setHand(player2, List.of(new GustOfWind()));
        harness.setLibrary(player2, List.of(new WingfoldPteron()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, original.getId());
        harness.assertNotOnBattlefield(player1, "Wingfold Pteron");
        harness.assertInHand(player1, "Wingfold Pteron");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "hexproof");

        Permanent returned = findPermanent(player1, "Wingfold Pteron");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(returned.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
        assertThat(returned.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(returned.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    private Permanent castAndChoose(String counterType) {
        harness.setHand(player1, List.of(new WingfoldPteron()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("flying", "hexproof");
        harness.handleListChoice(player1, counterType);

        return findPermanent(player1, "Wingfold Pteron");
    }
}
