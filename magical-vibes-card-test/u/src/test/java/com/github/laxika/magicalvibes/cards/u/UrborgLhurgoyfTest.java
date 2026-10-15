package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrborgLhurgoyf.class, GrizzlyBears.class, Forest.class})
class UrborgLhurgoyfTest extends BaseCardTest {

    @Test
    @DisplayName("Power counts creature cards in its controller's graveyard and toughness is one greater")
    void powerAndToughnessCountCreatureCardsInOwnGraveyard() {
        Permanent lhurgoyf = harness.addToBattlefieldAndReturn(player1, new UrborgLhurgoyf());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, lhurgoyf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lhurgoyf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Without kicker, it does not mill when it enters")
    void doesNotMillWithoutKicker() {
        castAndResolve(List.of(), List.of(new Forest(), new Forest(), new Forest()));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each kicker mills three cards, including when both are paid")
    void millsThreeCardsPerKicker() {
        castAndResolve(List.of("{U}", "{B}"), List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Forest", "Forest", "Forest", "Forest", "Forest");
    }

    @ParameterizedTest
    @ValueSource(strings = {"{U}", "{B}"})
    @DisplayName("Either individual kicker mills exactly three cards")
    void eitherKickerMillsThree(String kicker) {
        castAndResolve(List.of(kicker), List.of(
                new Forest(), new Forest(), new Forest(), new Forest()));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{U}", "{B}"})
    @DisplayName("Each kicker cost may only be paid once")
    void cannotPayTheSameKickerTwice(String kicker) {
        harness.setHand(player1, List.of(new UrborgLhurgoyf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, kicker.equals("{U}") ? ManaColor.BLUE : ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreatureWithRepeatedCosts(
                player1, 0, List.of(kicker, kicker)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Urborg Lhurgoyf");
    }

    @Test
    @DisplayName("Milling happens during entry without a separate ability on the stack")
    void millsBeforePlayersReceivePriorityAfterEntry() {
        harness.setHand(player1, List.of(new UrborgLhurgoyf()));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(
                new UrborgLhurgoyf(), new Forest(), new UrborgLhurgoyf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{U}"));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        Permanent lhurgoyf = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof UrborgLhurgoyf)
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, lhurgoyf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lhurgoyf)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Milling a short library mills all remaining cards without losing the game")
    void millsAllOfAShortLibrary() {
        castAndResolve(List.of("{U}", "{B}"), List.of(new Forest(), new UrborgLhurgoyf()));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Urborg Lhurgoyf");
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Power and toughness update when its controller's graveyard changes")
    void powerAndToughnessUpdateWithGraveyard() {
        Permanent lhurgoyf = harness.addToBattlefieldAndReturn(player1, new UrborgLhurgoyf());
        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, lhurgoyf)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, lhurgoyf)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new UrborgLhurgoyf(), new Forest()));
        assertThat(gqs.getEffectivePower(gd, lhurgoyf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lhurgoyf)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new Forest()));
        assertThat(gqs.getEffectivePower(gd, lhurgoyf)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, lhurgoyf)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"hand", "library", "graveyard"})
    @DisplayName("Its characteristic-defining power and toughness apply outside the battlefield")
    void powerAndToughnessApplyInOtherZones(String zone) {
        UrborgLhurgoyf lhurgoyf = new UrborgLhurgoyf();
        harness.setGraveyard(player1, List.of(new UrborgLhurgoyf(), new Forest()));
        harness.setGraveyard(player2, List.of(new UrborgLhurgoyf(), new UrborgLhurgoyf()));
        switch (zone) {
            case "hand" -> harness.setHand(player1, List.of(lhurgoyf));
            case "library" -> harness.setLibrary(player1, List.of(lhurgoyf));
            case "graveyard" -> harness.setGraveyard(player1, List.of(lhurgoyf, new Forest()));
            default -> throw new IllegalArgumentException(zone);
        }

        assertThat(gqs.getEffectiveCardPower(gd, lhurgoyf)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, lhurgoyf)).isEqualTo(2);
    }

    private void castAndResolve(List<String> kickerPayments, List<Card> library) {
        harness.setHand(player1, List.of(new UrborgLhurgoyf()));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        if (kickerPayments.contains("{U}")) {
            harness.addMana(player1, ManaColor.BLUE, 1);
        }
        if (kickerPayments.contains("{B}")) {
            harness.addMana(player1, ManaColor.BLACK, 1);
        }

        harness.castCreatureWithRepeatedCosts(player1, 0, kickerPayments);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
