package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BubbleSmuggler;
import com.github.laxika.magicalvibes.cards.d.Doppelgang;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KayaSpiritsJustice.class, Forest.class, BubbleSmuggler.class, Doppelgang.class})
class KayaSpiritsJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("+2 surveils, then mandatorily exiles a card from a graveyard")
    void plusTwoExilesARequiredGraveyardCard() {
        Permanent kaya = addReadyKaya(3);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player2, List.of(forest));

        activate(kaya, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(forest);
    }

    @Test
    @DisplayName("+1 creates a white and black flying Spirit")
    void plusOneCreatesSpirit() {
        Permanent kaya = addReadyKaya(3);

        activate(kaya, 1);
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
    }

    @Test
    @DisplayName("-2 exiles the controlled creature and at most one creature per opponent")
    void minusTwoExilesControlledAndOpponentCreatures() {
        Permanent kaya = addReadyKaya(3);
        Permanent ownCreature = addCreatureReady(player1, new BubbleSmuggler());
        Permanent opponentCreature = addCreatureReady(player2, new BubbleSmuggler());

        activateWithTargets(kaya, List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownCreature.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentCreature.getCard());
    }

    @Test
    @DisplayName("The exile trigger copies a chosen creature card onto a controlled token")
    void exileTriggerCopiesCreatureToTokenWithFlying() {
        Permanent kaya = addReadyKaya(3);
        activate(kaya, 1);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Spirit");
        Permanent creature = addCreatureReady(player1, new BubbleSmuggler());
        kaya.setLoyaltyActivationsThisTurn(0);

        activateWithTargets(kaya, List.of(creature.getId()));
        resolveCopyChoice(token, creature.getCard().getId());

        assertThat(token.getCard().getName()).isEqualTo("Bubble Smuggler");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(token.getCard().getName()).isEqualTo("Spirit");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    void plusTwoCanExileACardJustPutIntoTheGraveyardBySurveil() {
        Permanent kaya = addReadyKaya(3);
        Forest kept = new Forest();
        Forest exiled = new Forest();
        harness.setLibrary(player1, List.of(kept, exiled));

        activate(kaya, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));
        harness.handleMultipleCardsChosen(player1, List.of(exiled.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void plusTwoCannotDeclineExilingWhenAGraveyardCardIsAvailable() {
        Permanent kaya = addReadyKaya(3);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(forest));
        activate(kaya, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(forest);
    }

    @Test
    void minusTwoRequiresAControlledCreature() {
        Permanent kaya = addReadyKaya(3);
        Permanent opponent = addCreatureReady(player2, new BubbleSmuggler());

        assertThatThrownBy(() -> activateWithTargets(kaya, List.of(opponent.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void minusTwoCannotTargetTwoCreaturesOfTheSameOpponent() {
        Permanent kaya = addReadyKaya(3);
        Permanent own = addCreatureReady(player1, new BubbleSmuggler());
        Permanent first = addCreatureReady(player2, new BubbleSmuggler());
        Permanent second = addCreatureReady(player2, new BubbleSmuggler());

        assertThatThrownBy(() -> activateWithTargets(kaya, List.of(own.getId(), first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void minusTwoStillExilesTheOpponentCreatureWhenTheOwnTargetIsGone() {
        Permanent kaya = addReadyKaya(3);
        Permanent own = addCreatureReady(player1, new BubbleSmuggler());
        Permanent opponent = addCreatureReady(player2, new BubbleSmuggler());

        activateWithTargets(kaya, List.of(own.getId(), opponent.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(own);
        gd.playerGraveyards.get(player1.getId()).add(own.getCard());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponent.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(own.getCard());
    }

    @Test
    void exilingAnOpponentGraveyardCreatureDoesNotTriggerCopying() {
        Permanent kaya = addReadyKaya(3);
        activate(kaya, 1);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Spirit");
        BubbleSmuggler creature = new BubbleSmuggler();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player2, List.of(creature));
        kaya.setLoyaltyActivationsThisTurn(0);

        activate(kaya, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(token.getCard().getName()).isEqualTo("Spirit");
    }

    @Test
    void exileTriggerChoosesItsTokenTargetBeforeResolving() {
        Permanent kaya = addReadyKaya(3);
        activate(kaya, 1);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Spirit");
        Permanent creature = addCreatureReady(player1, new BubbleSmuggler());
        kaya.setLoyaltyActivationsThisTurn(0);

        activateWithTargets(kaya, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getCard().getId()));
        assertThat(token.getCard().getName()).isEqualTo("Bubble Smuggler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flyingIsCopiedByAnotherCopyEffect() {
        Permanent kaya = addReadyKaya(3);
        activate(kaya, 1);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Spirit");
        Permanent creature = addCreatureReady(player1, new BubbleSmuggler());
        kaya.setLoyaltyActivationsThisTurn(0);
        activateWithTargets(kaya, List.of(creature.getId()));
        resolveCopyChoice(token, creature.getCard().getId());

        harness.setHand(player1, List.of(new Doppelgang()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 1, List.of(token.getId()));
        harness.passBothPriorities();

        Permanent copy = findPermanents(player1, "Bubble Smuggler").stream()
                .filter(permanent -> !permanent.getId().equals(token.getId())).findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.FLYING)).isTrue();
    }

    @Test
    void creatureCardExiledFromOwnGraveyardCanBeCopied() {
        Permanent kaya = addReadyKaya(3);
        activate(kaya, 1);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Spirit");
        BubbleSmuggler creature = new BubbleSmuggler();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(creature));
        kaya.setLoyaltyActivationsThisTurn(0);

        activate(kaya, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        resolveCopyChoice(token, creature.getId());

        assertThat(token.getCard().getName()).isEqualTo("Bubble Smuggler");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    void choosingNoCreatureCardLeavesTheTokenUnchanged() {
        Permanent kaya = addReadyKaya(3);
        activate(kaya, 1);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Spirit");
        Permanent creature = addCreatureReady(player1, new BubbleSmuggler());
        kaya.setLoyaltyActivationsThisTurn(0);

        activateWithTargets(kaya, List.of(creature.getId()));
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, token.getId());
            resolveAllTriggers();
        }
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(token.getCard().getName()).isEqualTo("Spirit");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature.getCard());
    }

    private void resolveCopyChoice(Permanent token, java.util.UUID cardId) {
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, token.getId());
            resolveAllTriggers();
        }
        harness.handleMultipleCardsChosen(player1, List.of(cardId));
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, token.getId());
        }
        resolveAllTriggers();
    }

    private Permanent addReadyKaya(int loyalty) {
        Permanent kaya = addCreatureReady(player1, new KayaSpiritsJustice());
        kaya.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return kaya;
    }

    private void activate(Permanent kaya, int abilityIndex) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(kaya), abilityIndex,
                null, null);
    }

    private void activateWithTargets(Permanent kaya, List<java.util.UUID> targetIds) {
        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(kaya), 2, targetIds);
    }
}
