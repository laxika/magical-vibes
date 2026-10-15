package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.cards.v.VislorTurlough;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HotPursuit.class, SolemnSimulacrum.class, VislorTurlough.class})
class HotPursuitTest extends BaseCardTest {

    @Test
    @DisplayName("Suspects and goads the target creature while Hot Pursuit remains on the battlefield")
    void suspectsAndGoadsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolemnSimulacrum());

        castHotPursuit(target);

        assertThat(target.isSuspected()).isTrue();
        assertThat(gqs.isGoaded(gd, target)).isTrue();
    }

    @Test
    @DisplayName("At the beginning of combat, steals goaded and suspected creatures")
    void stealsGoadedAndSuspectedCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolemnSimulacrum());
        target.tap();
        Permanent suspectedOnly = harness.addToBattlefieldAndReturn(player2, new SolemnSimulacrum());
        suspectedOnly.setSuspected(true);
        castHotPursuit(target);
        recordFormerPlayerLoss();
        recordFormerPlayerLoss();

        advanceToCombat(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target, suspectedOnly);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target, suspectedOnly);
        assertThat(target.isTapped()).isFalse();
        assertThat(suspectedOnly.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, suspectedOnly, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.ensurePriority(player1);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target, suspectedOnly);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, suspectedOnly, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not steal creatures before two players have lost the game")
    void doesNotStealBeforeConditionIsMet() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolemnSimulacrum());
        castHotPursuit(target);
        recordFormerPlayerLoss();

        advanceToCombat(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void onlyTargetsAnOpponentsCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SolemnSimulacrum());
        harness.setHand(player1, List.of(new HotPursuit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void stillUntapsAndGrantsHasteWhenTakingControlEndsGoad() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolemnSimulacrum());
        castHotPursuit(target);
        harness.setHand(player1, List.of(new VislorTurlough()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        Permanent vislor = findPermanent(player2, "Vislor Turlough");
        assertThat(gqs.isGoaded(gd, vislor)).isTrue();
        assertThat(vislor.isSuspected()).isFalse();
        vislor.tap();
        recordFormerPlayerLoss();
        recordFormerPlayerLoss();

        advanceToCombat(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vislor);
        assertThat(gqs.isGoaded(gd, vislor)).isFalse();
        assertThat(vislor.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, vislor, Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotStealDuringOpponentsCombat() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolemnSimulacrum());
        castHotPursuit(target);
        target.tap();
        recordFormerPlayerLoss();
        recordFormerPlayerLoss();

        advanceToCombat(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    void doesNotAffectCreaturesThatAreNeitherGoadedNorSuspected() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolemnSimulacrum());
        Permanent unaffected = harness.addToBattlefieldAndReturn(player2, new SolemnSimulacrum());
        Permanent ownUnaffected = harness.addToBattlefieldAndReturn(player1, new SolemnSimulacrum());
        unaffected.tap();
        ownUnaffected.tap();
        castHotPursuit(target);
        recordFormerPlayerLoss();
        recordFormerPlayerLoss();

        advanceToCombat(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(unaffected);
        assertThat(unaffected.isTapped()).isTrue();
        assertThat(ownUnaffected.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, unaffected, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownUnaffected, Keyword.HASTE)).isFalse();
    }

    @Test
    void leavingBattlefieldEndsGoadButNotSuspectedStatus() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolemnSimulacrum());
        castHotPursuit(target);
        Permanent pursuit = findPermanent(player1, "Hot Pursuit");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, pursuit));
        harness.runStateBasedActions();

        assertThat(target.isSuspected()).isTrue();
        assertThat(gqs.isGoaded(gd, target)).isFalse();
    }

    @Test
    void leavingBeforeEntryTriggerResolvesStillSuspectsButDoesNotGoad() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolemnSimulacrum());
        harness.setHand(player1, List.of(new HotPursuit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent pursuit = findPermanent(player1, "Hot Pursuit");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, pursuit));

        resolveAllTriggers();

        assertThat(target.isSuspected()).isTrue();
        assertThat(gqs.isGoaded(gd, target)).isFalse();
    }

    private void recordFormerPlayerLoss() {
        java.util.UUID formerPlayerId = java.util.UUID.randomUUID();
        gd.playerIds.add(formerPlayerId);
        gd.playersWhoLostGameThisMatch.add(formerPlayerId);
    }
    private void castHotPursuit(Permanent target) {
        harness.setHand(player1, List.of(new HotPursuit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }
}
