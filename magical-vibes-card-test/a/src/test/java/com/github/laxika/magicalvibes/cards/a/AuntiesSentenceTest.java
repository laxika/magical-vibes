package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuntiesSentence.class, GrizzlyBears.class, Peek.class, Forest.class, SpringleafDrum.class})
class AuntiesSentenceTest extends BaseCardTest {

    @Test
    @DisplayName("Creature mode gives target creature -2/-2 until end of turn")
    void creatureModeGivesMinusTwoMinusTwo() {
        GrizzlyBears bearCard = new GrizzlyBears();
        bearCard.setPower(4);
        bearCard.setToughness(4);
        Permanent bear = addCreatureReady(player2, bearCard);

        harness.setHand(player1, List.of(new AuntiesSentence()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(-2);
        assertThat(bear.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Hand mode discards a chosen nonland permanent card")
    void handModeDiscardsChosenNonlandPermanent() {
        Card bear = new GrizzlyBears();
        Card peek = new Peek();
        Card forest = new Forest();
        harness.setHand(player2, List.of(bear, peek, forest));

        harness.setHand(player1, List.of(new AuntiesSentence()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Peek");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    void handModeCanDiscardANoncreaturePermanent() {
        Card artifact = new SpringleafDrum();
        harness.setHand(player2, List.of(artifact, new AuntiesSentence()));
        harness.setHand(player1, List.of(new AuntiesSentence()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Springleaf Drum");
        harness.assertNotInHand(player2, "Springleaf Drum");
        harness.assertInHand(player2, "Auntie's Sentence");
    }

    @Test
    void handModeCannotTargetController() {
        harness.setHand(player1, List.of(new AuntiesSentence()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void creatureModeCannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AuntiesSentence()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void handModeResolvesAgainstEmptyHand() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new AuntiesSentence()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Auntie's Sentence");
    }

    @Test
    void handModeRevealsButDoesNotDiscardWhenNoNonlandPermanentExists() {
        Card instant = new Peek();
        Card sorcery = new AuntiesSentence();
        Card land = new Forest();
        harness.setHand(player2, List.of(instant, sorcery, land));
        harness.setHand(player1, List.of(new AuntiesSentence()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(instant, sorcery, land);
        assertThat(gameLogContains("reveals their hand")).isTrue();
        harness.assertInGraveyard(player1, "Auntie's Sentence");
    }

    @Test
    void controllerChoosesExactlyOneOfMultipleEligibleCards() {
        Card firstBear = new GrizzlyBears();
        Card secondBear = new GrizzlyBears();
        harness.setHand(player2, List.of(firstBear, secondBear));
        harness.setHand(player1, List.of(new AuntiesSentence()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstBear);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(secondBear).doesNotContain(firstBear);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void creatureModeCanKillControllersOwnCreature() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AuntiesSentence()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 1, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void creaturePenaltyExpiresAtEndOfTurnAndLeavesCountersIntact() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new AuntiesSentence()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 1, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
