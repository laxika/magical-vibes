package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FinalDeath;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NadirKraken.class, NyxbornColossus.class, FinalDeath.class})
class NadirKrakenTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} after drawing adds a counter and creates a Tentacle")
    void payingAfterDrawingAddsCounterAndCreatesTentacle() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new NadirKraken());
        harness.setLibrary(player1, List.of(new NyxbornColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        draw();
        resolveTopOfStack();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(kraken.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, kraken)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kraken)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        Permanent tentacle = findPermanent(player1, "Tentacle");
        assertThat(tentacle.getEffectivePower()).isEqualTo(1);
        assertThat(tentacle.getEffectiveToughness()).isEqualTo(1);
        assertThat(tentacle.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(tentacle.getCard().getSubtypes()).containsExactly(CardSubtype.TENTACLE);
    }

    @Test
    @DisplayName("Declining the payment after drawing does nothing")
    void decliningPaymentDoesNothing() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new NadirKraken());
        harness.setLibrary(player1, List.of(new NyxbornColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        draw();
        resolveTopOfStack();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(kraken.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Tentacle")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("A single draw only permits one payment even with extra mana")
    void singleDrawOnlyPermitsOnePayment() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new NadirKraken());
        harness.setLibrary(player1, List.of(new NyxbornColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        draw();
        resolveTopOfStack();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(kraken.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Tentacle")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Accepting without mana gives neither a counter nor a token")
    void cannotPayWithoutMana() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new NadirKraken());
        harness.setLibrary(player1, List.of(new NyxbornColossus()));

        draw();
        resolveTopOfStack();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(kraken.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Tentacle")).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent drawing does not trigger Nadir Kraken")
    void opponentDrawDoesNotTrigger() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new NadirKraken());
        harness.setLibrary(player2, List.of(new NyxbornColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(kraken.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Tentacle")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Drawing multiple cards completes all draws before independent payment choices")
    void multipleDrawsHaveIndependentPayments() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new NadirKraken());
        harness.setLibrary(player1, List.of(new NyxbornColossus(), new NyxbornColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        resolveTopOfStack();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(kraken.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Tentacle")).isEmpty();

        resolveTopOfStack();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(kraken.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Tentacle")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The token is still created if the Kraken leaves before its trigger resolves")
    void paymentCreatesTokenAfterKrakenLeavesBattlefield() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new NadirKraken());
        harness.setLibrary(player1, List.of(new NyxbornColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new FinalDeath()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        draw();
        harness.castAndResolveInstant(player2, 0, kraken.getId());
        harness.assertNotOnBattlefield(player1, "Nadir Kraken");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Tentacle")).hasSize(1);
        assertThat(findPermanents(player2, "Tentacle")).isEmpty();
        assertThat(kraken.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private void draw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
