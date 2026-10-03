package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeetleHeadedMerchants.class, GrizzlyBears.class, MindStone.class})
class BeetleHeadedMerchantsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature draws a card and adds a +1/+1 counter")
    void sacrificingAnotherCreatureDrawsAndAddsCounter() {
        Permanent merchants = addCreatureReady(player1, new BeetleHeadedMerchants());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(merchants.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing another artifact draws a card and adds a +1/+1 counter")
    void sacrificingAnotherArtifactDrawsAndAddsCounter() {
        Permanent merchants = addCreatureReady(player1, new BeetleHeadedMerchants());
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, mindStone.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(mindStone.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(merchants.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the sacrifice does nothing")
    void decliningTheSacrificeDoesNothing() {
        Permanent merchants = addCreatureReady(player1, new BeetleHeadedMerchants());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(merchants.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The source and opposing creatures cannot be sacrificed")
    void noOtherControlledPermanentGivesNoReward() {
        Permanent merchants = addCreatureReady(player1, new BeetleHeadedMerchants());
        Permanent opponentCreature = addCreatureReady(player2, new BeetleHeadedMerchants());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BeetleHeadedMerchants()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(merchants);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(merchants.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Another copy can be sacrificed but opposing creatures cannot")
    void anotherCopyIsAnEligibleSacrifice() {
        Permanent merchants = addCreatureReady(player1, new BeetleHeadedMerchants());
        Permanent otherMerchants = addCreatureReady(player1, new BeetleHeadedMerchants());
        Permanent opponentCreature = addCreatureReady(player2, new BeetleHeadedMerchants());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BeetleHeadedMerchants()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(otherMerchants.getId());
        harness.handlePermanentChosen(player1, otherMerchants.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherMerchants.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(merchants.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger still draws a card if its source leaves the battlefield")
    void sourceLeavingDoesNotPreventSacrificeAndDraw() {
        Permanent merchants = addCreatureReady(player1, new BeetleHeadedMerchants());
        Permanent otherMerchants = addCreatureReady(player1, new BeetleHeadedMerchants());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BeetleHeadedMerchants()));

        declareAttackers(List.of(0));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, merchants));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, otherMerchants.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(merchants.getCard(), otherMerchants.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(merchants.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherMerchants.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
