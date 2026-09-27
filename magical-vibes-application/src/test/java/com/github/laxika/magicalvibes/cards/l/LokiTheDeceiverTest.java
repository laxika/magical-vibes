package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DoctorDoomKingOfLatveria;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LokiTheDeceiver.class, DoctorDoomKingOfLatveria.class, GiantGrowth.class, GrizzlyBears.class})
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
        assertThat(copy.isAttackedThisTurn()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(loki.getAttackTarget());
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(copy.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
    }

    @Test
    @DisplayName("The attack trigger only targets another Villain you control")
    void attackTriggerOnlyTargetsAnotherControlledVillain() {
        Permanent loki = addCreatureReady(player1, new LokiTheDeceiver());
        Permanent ownVillain = addCreatureReady(player1, new DoctorDoomKingOfLatveria());
        Permanent opponentVillain = addCreatureReady(player2, new DoctorDoomKingOfLatveria());
        addCreatureReady(player1, new GrizzlyBears());
        keepCombatOpen();

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownVillain.getId())
                .doesNotContain(loki.getId(), opponentVillain.getId());
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

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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

    private void chooseTarget(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
    }

    private void keepCombatOpen() {
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
    }
}
