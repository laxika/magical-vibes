package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.n.NervousGardener;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cryptex.class, NervousGardener.class})
class CryptexTest extends BaseCardTest {

    @Test
    void cannotCollectEvidenceWithInsufficientGraveyardManaValue() {
        Permanent cryptex = harness.addToBattlefieldAndReturn(player1, new Cryptex());
        Card evidence = new NervousGardener();
        harness.setGraveyard(player1, List.of(evidence));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cryptex.isTapped()).isFalse();
        assertThat(cryptex.getCounterCount(CounterType.UNLOCK)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(evidence);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsEvidenceSelectionBelowThreeEvenWhenEnoughEvidenceIsAvailable() {
        Permanent cryptex = harness.addToBattlefieldAndReturn(player1, new Cryptex());
        Card firstEvidence = new NervousGardener();
        Card secondEvidence = new NervousGardener();
        harness.setGraveyard(player1, List.of(firstEvidence, secondEvidence));
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(firstEvidence.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cryptex.isTapped()).isFalse();
        assertThat(cryptex.getCounterCount(CounterType.UNLOCK)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstEvidence, secondEvidence);
    }

    @Test
    void tappedCryptexWithMoreThanFiveCountersCanBeSacrificedAndKeepAllSurveilledCards() {
        Permanent cryptex = harness.addToBattlefieldAndReturn(player1, new Cryptex());
        cryptex.tap();
        cryptex.setCounterCount(CounterType.UNLOCK, 6);
        Card first = new NervousGardener();
        Card second = new NervousGardener();
        Card third = new NervousGardener();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cryptex);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cryptex.getCard());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(2, 0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third, first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cryptex.getCard());
    }

    @Test
    void collectsEvidenceAddsManaAndUnlockCounter() {
        Card firstEvidence = new NervousGardener();
        Card secondEvidence = new NervousGardener();
        Permanent cryptex = harness.addToBattlefieldAndReturn(player1, new Cryptex());
        harness.setGraveyard(player1, List.of(firstEvidence, secondEvidence));

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(firstEvidence.getId(), secondEvidence.getId()));
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(cryptex.getCounterCount(CounterType.UNLOCK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void sacrificeAbilityRequiresFiveUnlockCounters() {
        Permanent cryptex = harness.addToBattlefieldAndReturn(player1, new Cryptex());
        cryptex.setCounterCount(CounterType.UNLOCK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unlock counters");
    }

    @Test
    void sacrificesSurveilsAndDrawsThreeCardsWithFiveUnlockCounters() {
        Permanent cryptex = harness.addToBattlefieldAndReturn(player1, new Cryptex());
        cryptex.setCounterCount(CounterType.UNLOCK, 5);
        Card surveilled = new NervousGardener();
        Card kept = new NervousGardener();
        Card firstDraw = new NervousGardener();
        Card secondDraw = new NervousGardener();
        Card thirdDraw = new NervousGardener();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(surveilled, firstDraw, secondDraw, thirdDraw, kept));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 2), List.of(0)));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cryptex);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(cryptex.getCard(), surveilled);
    }
}
