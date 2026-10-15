package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DoctorDoomKingOfLatveria;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LokiTheDeceiver.class, DoctorDoomKingOfLatveria.class, GiantGrowth.class, GrizzlyBears.class, JaceBeleren.class})
class LokiTheDeceiverTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking nonlegendary Illusion copy")
    void attackingCreatesTappedAttackingIllusionCopy() {
        Permanent loki = addCreatureReady(player1, new LokiTheDeceiver());
        Permanent doom = addCreatureReady(player1, new DoctorDoomKingOfLatveria());
        keepCombatOpen();

        declareAttackers(List.of(0));
        chooseTarget(doom);
        resolveAllTriggers();

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(copy.getCard().getName()).isEqualTo(doom.getCard().getName());
        assertThat(copy.getCard().getSubtypes()).contains(CardSubtype.ILLUSION);
        assertThat(copy.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.isAttackedThisTurn()).isFalse();
        assertThat(copy.getAttackTarget()).isEqualTo(loki.getAttackTarget());
    }

    @Test
    @DisplayName("The attack trigger only targets another Villain you control")
    void attackTriggerOnlyTargetsAnotherControlledVillain() {
        Permanent loki = addCreatureReady(player1, new LokiTheDeceiver());
        Permanent ownVillain = addCreatureReady(player1, new DoctorDoomKingOfLatveria());
        Permanent opponentVillain = addCreatureReady(player2, new DoctorDoomKingOfLatveria());
        Permanent nonVillain = addCreatureReady(player1, new GrizzlyBears());
        keepCombatOpen();

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownVillain.getId())
                .doesNotContain(loki.getId(), opponentVillain.getId(), nonVillain.getId());
    }

    @Test
    @DisplayName("The created copy is sacrificed at the beginning of the next end step")
    void createdCopyIsSacrificedAtNextEndStep() {
        addCreatureReady(player1, new LokiTheDeceiver());
        Permanent doom = addCreatureReady(player1, new DoctorDoomKingOfLatveria());
        keepCombatOpen();

        declareAttackers(List.of(0));
        chooseTarget(doom);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("One or more Villains dealing combat damage draws one card")
    void oneOrMoreVillainsDealCombatDamageDrawsOnce() {
        addCreatureReady(player1, new LokiTheDeceiver());
        Permanent doom = addCreatureReady(player1, new DoctorDoomKingOfLatveria());
        keepCombatOpen();

        declareAttackers(List.of(0, 1));
        chooseTarget(doom);
        resolveAllTriggers();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("The token's controller may choose a planeswalker instead of Loki's defender")
    void tokenMayAttackADifferentDefender() {
        addCreatureReady(player1, new LokiTheDeceiver());
        Permanent doom = addCreatureReady(player1, new DoctorDoomKingOfLatveria());
        Permanent jace = harness.enterBattlefieldAndReturn(player2, new JaceBeleren());
        keepCombatOpen();

        declareAttackers(List.of(0));
        chooseTarget(doom);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(player2.getId(), jace.getId());
        chooseTarget(jace);

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(jace.getId());
    }

    @Test
    @DisplayName("Loki's own combat damage draws a card even without another Villain")
    void lokisOwnCombatDamageDraws() {
        addCreatureReady(player1, new LokiTheDeceiver());
        keepCombatOpen();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Combat damage from a non-Villain does not draw a card")
    void nonVillainCombatDamageDoesNotDraw() {
        addCreatureReady(player1, new LokiTheDeceiver());
        addCreatureReady(player1, new GrizzlyBears());
        keepCombatOpen();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("No copy is created if the targeted Villain leaves before resolution")
    void missingTargetDoesNotCreateCopy() {
        addCreatureReady(player1, new LokiTheDeceiver());
        Permanent doom = addCreatureReady(player1, new DoctorDoomKingOfLatveria());
        keepCombatOpen();

        declareAttackers(List.of(0));
        chooseTarget(doom);
        gd.playerBattlefields.get(player1.getId()).remove(doom);
        gd.playerGraveyards.get(player1.getId()).add(doom.getCard());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The delayed sacrifice uses the stack at the beginning of the end step")
    void sacrificeCanBeRespondedTo() {
        addCreatureReady(player1, new LokiTheDeceiver());
        Permanent doom = addCreatureReady(player1, new DoctorDoomKingOfLatveria());
        keepCombatOpen();

        declareAttackers(List.of(0));
        chooseTarget(doom);
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("A token controlled by another player is not sacrificed by Loki's delayed ability")
    void stolenTokenIsNotSacrificed() {
        addCreatureReady(player1, new LokiTheDeceiver());
        Permanent doom = addCreatureReady(player1, new DoctorDoomKingOfLatveria());
        keepCombatOpen();

        declareAttackers(List.of(0));
        chooseTarget(doom);
        resolveAllTriggers();
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();

        resolveCombat();
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(copy);
        gd.playerBattlefields.get(player2.getId()).add(copy);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(copy);
    }

    private void chooseTarget(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
    }

    private void keepCombatOpen() {
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
    }
}
