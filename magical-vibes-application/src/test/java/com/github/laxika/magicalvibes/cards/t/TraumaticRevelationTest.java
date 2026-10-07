package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.IchorDrinker;
import com.github.laxika.magicalvibes.cards.d.DeadlyDerision;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({TraumaticRevelation.class, IchorDrinker.class, DeadlyDerision.class, InvasionOfZendikar.class})
class TraumaticRevelationTest extends BaseCardTest {

    @Test
    void choosesAndDiscardsCreatureFromOpponentHand() {
        harness.setHand(player2, List.of(new IchorDrinker(), new DeadlyDerision()));
        cast();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Ichor Drinker");
        assertThat(gd.playerHands.get(player2.getId())).singleElement()
                .extracting(card -> card.getName()).isEqualTo("Deadly Derision");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void decliningChoiceIncubatesThree() {
        harness.setHand(player2, List.of(new IchorDrinker(), new DeadlyDerision()));
        cast();

        harness.handleCardChosen(player1, -1);

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void noMatchingCardIncubatesWithoutPrompt() {
        harness.setHand(player2, List.of(new DeadlyDerision()));
        cast();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Deadly Derision");
    }

    @Test
    void canOnlyTargetOpponent() {
        harness.setHand(player1, List.of(new TraumaticRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void choosesBattleWithoutIncubatingOrUsingItsBackFaceInHand() {
        harness.setHand(player2, List.of(new InvasionOfZendikar(), new IchorDrinker(), new DeadlyDerision()));
        cast();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0, 1);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Invasion of Zendikar");
        harness.assertInHand(player2, "Ichor Drinker");
        harness.assertInHand(player2, "Deadly Derision");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyHandIncubatesExactlyOneTokenWithoutPrompt() {
        harness.setHand(player2, List.of());
        cast();

        assertThat(countPermanents(player1, "Incubator")).isEqualTo(1);
        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, incubator)).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotChooseAnInstantFromRevealedHand() {
        harness.setHand(player2, List.of(new IchorDrinker(), new DeadlyDerision()));
        cast();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player2, "Ichor Drinker");
        harness.assertInHand(player2, "Deadly Derision");
        harness.assertNotOnBattlefield(player1, "Incubator");
    }

    @Test
    void incubatorTransformsIntoThreeThreeAndRetainsCounters() {
        harness.setHand(player2, List.of(new IchorDrinker()));
        cast();
        harness.handleCardChosen(player1, -1);
        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(gqs.isCreature(gd, incubator)).isFalse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        assertThat(incubator.isTransformed()).isFalse();
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(gqs.isArtifact(gd, incubator)).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(3);
        harness.assertInHand(player2, "Ichor Drinker");
    }

    private void cast() {
        harness.setHand(player1, List.of(new TraumaticRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
