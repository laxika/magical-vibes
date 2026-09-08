package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PutridCyclops.class, Forest.class, GrizzlyBears.class})
class PutridCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Scrying keeps the top card and gives Putrid Cyclops -X/-X by its mana value")
    void keepsTopCardAndGetsMinusByItsManaValue() {
        Card cyclopsCard = new PutridCyclops();
        Card topCard = new GrizzlyBears();
        harness.setHand(player1, List.of(cyclopsCard));
        harness.setLibrary(player1, List.of(topCard, new Forest()));
        addCyclopsMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        Permanent cyclops = findCyclops(cyclopsCard);
        assertThat(cyclops.getEffectivePower()).isEqualTo(1);
        assertThat(cyclops.getEffectiveToughness()).isEqualTo(1);
        assertThat(cyclops.getPowerModifier()).isEqualTo(-2);
        assertThat(cyclops.getToughnessModifier()).isEqualTo(-2);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("The card left on top after scry determines the penalty")
    void bottomedCardIsNotUsedForPenalty() {
        Card cyclopsCard = new PutridCyclops();
        Card scriedCard = new Forest();
        Card revealedCard = new GrizzlyBears();
        harness.setHand(player1, List.of(cyclopsCard));
        harness.setLibrary(player1, List.of(scriedCard, revealedCard));
        addCyclopsMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        Permanent cyclops = findCyclops(cyclopsCard);
        assertThat(cyclops.getEffectivePower()).isEqualTo(1);
        assertThat(cyclops.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(revealedCard);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(scriedCard);
    }

    @Test
    @DisplayName("The temporary penalty wears off at end of turn")
    void penaltyWearsOffAtEndOfTurn() {
        Card cyclopsCard = new PutridCyclops();
        harness.setHand(player1, List.of(cyclopsCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclopsMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        Permanent cyclops = findCyclops(cyclopsCard);
        assertThat(cyclops.getEffectivePower()).isEqualTo(1);
        assertThat(cyclops.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cyclops.getEffectivePower()).isEqualTo(3);
        assertThat(cyclops.getEffectiveToughness()).isEqualTo(3);
    }

    private void addCyclopsMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private Permanent findCyclops(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }
}
