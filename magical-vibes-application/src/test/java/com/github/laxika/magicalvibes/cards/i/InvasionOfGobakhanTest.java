package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DregRecycler;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightshieldArray;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExilePlayCostModifier;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        DregRecycler.class,
        Forest.class,
        InvasionOfGobakhan.class,
        LightshieldArray.class,
        Plains.class
})
class InvasionOfGobakhanTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at an opponent's hand and may exile a nonland card with its owner's permission and a tax")
    void looksAtHandAndExilesNonlandWithOwnerPermissionAndTax() {
        Card nonland = new DregRecycler();
        Card plains = new Plains();
        harness.setHand(player2, List.of(nonland, plains));

        castInvasion();
        resolveAllTriggers();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);
        assertThat(choice.optional()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(nonland);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(plains);
        assertThat(gd.exilePlayPermissions.get(nonland.getId())).isEqualTo(player2.getId());
        ExilePlayCostModifier modifier = gd.exilePlayCostModifiers.get(nonland.getId());
        assertThat(modifier.permittedPlayerId()).isEqualTo(player2.getId());
        assertThat(modifier.sourceControllerId()).isEqualTo(player1.getId());
        assertThat(modifier.amount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Defeating the Siege exiles it and casts Lightshield Array transformed")
    void defeatCastsBackFace() {
        harness.setHand(player2, List.of(new Forest()));
        castInvasion();
        resolveAllTriggers();

        Permanent battle = findPermanent(player1, "Invasion of Gobakhan");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        Permanent backFace = findPermanent(player1, "Lightshield Array");
        assertThat(backFace.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Lightshield Array puts counters on creatures that attacked this turn at end step")
    void endStepCountersAttackers() {
        harness.addToBattlefield(player1, new LightshieldArray());
        Permanent attacker = addCreatureReady(player1, new DregRecycler());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing Lightshield Array protects creatures you control until end of turn")
    void sacrificeGrantsHexproofAndIndestructible() {
        Permanent array = harness.addToBattlefieldAndReturn(player1, new LightshieldArray());
        Permanent creature = addCreatureReady(player1, new DregRecycler());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(array);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void handLookIsPrivateAndDoesNotPublishUnchosenCards() {
        harness.setHand(player2, List.of(new DregRecycler(), new Plains()));
        castInvasion();
        harness.clearMessages();
        resolveAllTriggers();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND")).hasSize(1);
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains("reveals their hand"));
        harness.handleCardChosen(player1, -1);
    }

    @Test
    void mayDeclineToExileANonlandCard() {
        Card card = new DregRecycler();
        harness.setHand(player2, List.of(card));
        castInvasion();
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landOnlyAndEmptyHandsRequireNoChoice() {
        Card land = new Plains();
        harness.setHand(player2, List.of(land));
        castInvasion();
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();

        harness.setHand(player2, List.of());
        castInvasion();
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ownerCanCastExiledCardWithTaxAfterSiegeLeaves() {
        Card card = new DregRecycler();
        harness.setHand(player2, List.of(card));
        castInvasion();
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        Permanent battle = findPermanent(player1, "Invasion of Gobakhan");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, battle));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromExile(player2, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castFromExile(player2, card.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Dreg Recycler");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void exilePermissionDoesNotLetTheSiegeControllerCastTheOpponentsCard() {
        Card card = new DregRecycler();
        harness.setHand(player2, List.of(card));
        castInvasion();
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
    }

    @Test
    void exilePermissionDoesNotOverrideCreatureTimingRestrictions() {
        Card card = new DregRecycler();
        harness.setHand(player2, List.of(card));
        castInvasion();
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromExile(player2, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
    }

    @Test
    void defeatAllowsDecliningTheTransformedCast() {
        harness.setHand(player2, List.of());
        castInvasion();
        resolveAllTriggers();
        Permanent battle = findPermanent(player1, "Invasion of Gobakhan");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(battle.getCard());
        harness.assertNotOnBattlefield(player1, "Lightshield Array");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void endStepCountsPreviousAttackersAcrossBothControllersButNotNonattackers() {
        harness.addToBattlefield(player1, new LightshieldArray());
        Permanent ownAttacker = addCreatureReady(player1, new DregRecycler());
        Permanent opposingAttacker = addCreatureReady(player2, new DregRecycler());
        Permanent nonattacker = addCreatureReady(player1, new DregRecycler());
        ownAttacker.setAttackedThisTurn(true);
        opposingAttacker.setAttackedThisTurn(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(ownAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonattacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new LightshieldArray());
        Permanent attacker = addCreatureReady(player2, new DregRecycler());
        attacker.setAttackedThisTurn(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void protectionUsesCreaturesAtResolutionAndExpiresAtCleanup() {
        Permanent array = harness.addToBattlefieldAndReturn(player1, new LightshieldArray());
        Permanent original = addCreatureReady(player1, new DregRecycler());
        Permanent opponent = addCreatureReady(player2, new DregRecycler());
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(array);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new DregRecycler());
        resolveAllTriggers();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new DregRecycler());

        for (Permanent creature : List.of(original, beforeResolution)) {
            assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
        }
        for (Permanent creature : List.of(opponent, afterResolution)) {
            assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        }

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, original, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, original, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void castInvasion() {
        harness.setHand(player1, List.of(new InvasionOfGobakhan()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gs.playCard(gd, player1, 0, 0, player2.getId(), null);
    }
}
