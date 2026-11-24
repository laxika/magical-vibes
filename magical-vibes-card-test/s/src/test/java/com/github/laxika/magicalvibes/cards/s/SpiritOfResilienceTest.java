package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.p.PerpetualTimepiece;
import com.github.laxika.magicalvibes.cards.r.Regrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiritOfResilience.class, Regrowth.class, HillGiant.class, MindStone.class, PerpetualTimepiece.class})
class SpiritOfResilienceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts on a counter, then may become a copy of a creature card that left the graveyard")
    void putsCounterAndCopiesCreatureCard() {
        Permanent spirit = addSpirit();
        HillGiant giant = new HillGiant();
        returnCardFromGraveyard(giant);

        resolveTrigger();

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));

        assertThat(spirit.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the copy still keeps the counter")
    void decliningCopyKeepsCounter() {
        Permanent spirit = addSpirit();
        returnCardFromGraveyard(new HillGiant());

        resolveTrigger();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(spirit.getCard().getName()).isEqualTo("Spirit of Resilience");
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can become a copy of an artifact card that left the graveyard")
    void copiesArtifactCard() {
        Permanent spirit = addSpirit();
        MindStone stone = new MindStone();
        returnCardFromGraveyard(stone);

        resolveTrigger();
        harness.handleMultipleCardsChosen(player1, List.of(stone.getId()));

        assertThat(spirit.getCard().getName()).isEqualTo("Mind Stone");
    }

    @Test
    @DisplayName("The copy wears off at end of turn")
    void copyWearsOffAtEndOfTurn() {
        Permanent spirit = addSpirit();
        HillGiant giant = new HillGiant();
        returnCardFromGraveyard(giant);

        resolveTrigger();
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));

        assertThat(spirit.getCard().getName()).isEqualTo("Hill Giant");

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(spirit.getCard().getName()).isEqualTo("Spirit of Resilience");
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void nonArtifactNonCreatureCardStillAddsCounterWithoutCopyChoice() {
        Permanent spirit = addSpirit();
        returnCardFromGraveyard(new Regrowth());

        resolveTrigger();

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(spirit.getCard().getName()).isEqualTo("Spirit of Resilience");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void separateGraveyardDeparturesEachAddCounter() {
        Permanent spirit = addSpirit();
        returnCardFromGraveyard(new HillGiant());
        resolveTrigger();
        harness.handleMultipleCardsChosen(player1, List.of());

        returnCardFromGraveyard(new HillGiant());
        resolveTrigger();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void copiedCreatureNoLongerTriggersOnGraveyardDepartures() {
        Permanent spirit = addSpirit();
        HillGiant giant = new HillGiant();
        returnCardFromGraveyard(giant);
        resolveTrigger();
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));

        returnCardFromGraveyard(new MindStone());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsGraveyardDepartureDoesNotTrigger() {
        Permanent spirit = addSpirit();
        HillGiant giant = new HillGiant();
        harness.setGraveyard(player2, List.of(giant));
        harness.setHand(player2, List.of(new Regrowth()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player2, 0, giant.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void simultaneousDeparturesAddOneCounterAndAllowChoosingEitherArtifact() {
        Permanent spirit = addSpirit();
        MindStone stone = new MindStone();
        PerpetualTimepiece timepiece = new PerpetualTimepiece();
        shuffleCardsFromGraveyard(stone, timepiece);

        resolveTrigger();
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.cards()).containsExactly(stone, timepiece);
        harness.handleMultipleCardsChosen(player1, List.of(timepiece.getId()));
        harness.passBothPriorities();

        assertThat(spirit.getCard().getName()).isEqualTo("Perpetual Timepiece");
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing one of several cards copies it as the ability resolves, with nothing put on the stack")
    void choosingAmongSeveralCardsCopiesAsTheAbilityResolves() {
        Permanent spirit = addSpirit();
        MindStone stone = new MindStone();
        PerpetualTimepiece timepiece = new PerpetualTimepiece();
        shuffleCardsFromGraveyard(stone, timepiece);

        resolveTrigger();
        harness.handleMultipleCardsChosen(player1, List.of(stone.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(spirit.getCard().getName()).isEqualTo("Mind Stone");
    }

    @Test
    void laterBatchCannotCopyCardsFromAnEarlierBatch() {
        Permanent spirit = addSpirit();
        shuffleCardsFromGraveyard(new MindStone());
        resolveTrigger();
        harness.handleMultipleCardsChosen(player1, List.of());

        PerpetualTimepiece timepiece = new PerpetualTimepiece();
        shuffleCardsFromGraveyard(timepiece);
        resolveTrigger();
        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.cards()).containsExactly(timepiece);
        harness.handleMultipleCardsChosen(player1, List.of(timepiece.getId()));

        assertThat(spirit.getCard().getName()).isEqualTo("Perpetual Timepiece");
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void shuffleCardsFromGraveyard(Card... cards) {
        harness.setGraveyard(player1, List.of(cards));
        Permanent timepiece = harness.addToBattlefieldAndReturn(player1, new PerpetualTimepiece());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbilityWithGraveyardTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(timepiece), 1,
                java.util.Arrays.stream(cards).map(Card::getId).toList());
        harness.passBothPriorities();
    }

    private Permanent addSpirit() {
        return harness.addToBattlefieldAndReturn(player1, new SpiritOfResilience());
    }

    private void returnCardFromGraveyard(Card card) {
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new Regrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, card.getId());
    }

    private void resolveTrigger() {
        harness.passBothPriorities();
    }
}
