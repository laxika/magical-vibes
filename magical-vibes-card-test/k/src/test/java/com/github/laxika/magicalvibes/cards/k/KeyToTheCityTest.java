package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DramaticReversal;
import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeyToTheCity.class, DukharaPeafowl.class, Forest.class, DramaticReversal.class})
class KeyToTheCityTest extends BaseCardTest {

    @Test
    void discardingMakesTargetCreatureUnblockableUntilEndOfTurn() {
        Permanent key = addReadyKey(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DukharaPeafowl());
        harness.setHand(player1, List.of(new DukharaPeafowl()));

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(key.isTapped()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
        harness.assertInGraveyard(player1, "Dukhara Peafowl");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void mayActivateWithoutChoosingATarget() {
        addReadyKey(player1);
        harness.setHand(player1, List.of(new DukharaPeafowl()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingWhenKeyBecomesUntappedDrawsACard() {
        Permanent key = addTappedKey(player1);
        harness.setLibrary(player1, List.of(new Forest()));

        runUntapStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThat(key.isTapped()).isFalse();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void decliningUntapTriggerDoesNotDraw() {
        addTappedKey(player1);
        harness.setLibrary(player1, List.of(new Forest(), new DukharaPeafowl()));

        runUntapStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        Permanent key = addReadyKey(player1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(key.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void untappingWithASpellAllowsPayingToDraw() {
        Permanent key = addTappedKey(player1);
        harness.setHand(player1, List.of(new DramaticReversal()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);
        assertThat(key.isTapped()).isFalse();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void untappingAnAlreadyUntappedKeyDoesNotTrigger() {
        addReadyKey(player1);
        harness.setHand(player1, List.of(new DramaticReversal()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void insufficientManaDoesNotDrawOrSpendPartialPayment() {
        addTappedKey(player1);
        harness.setHand(player1, List.of(new DramaticReversal()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void untapTriggerDrawsForTheKeysController() {
        addTappedKey(player2);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        runUntapStep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    private Permanent addReadyKey(Player player) {
        Permanent key = harness.addToBattlefieldAndReturn(player, new KeyToTheCity());
        key.setSummoningSick(false);
        return key;
    }

    private Permanent addTappedKey(Player player) {
        Permanent key = addReadyKey(player);
        key.tap();
        return key;
    }

    private void runUntapStep(Player untappingPlayer) {
        Player opponent = untappingPlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(untappingPlayer, TurnStep.UPKEEP);
    }
}
