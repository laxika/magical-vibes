package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DrownyardExplorers;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElusiveTormentor.class, DrownyardExplorers.class, FieryTemper.class})
class ElusiveTormentorTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card and paying {1} transforms Elusive Tormentor")
    void discardAndPayTransformsTormentor() {
        Permanent tormentor = addReadyTormentor();
        harness.setHand(player1, List.of(new DrownyardExplorers()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(tormentor.isTransformed()).isTrue();
        assertThat(tormentor.getCard().getName()).isEqualTo("Insidious Mist");
        harness.assertInGraveyard(player1, "Drownyard Explorers");
    }

    @Test
    @DisplayName("Insidious Mist may pay {2}{B} after attacking unblocked to transform back")
    void unblockedAttackMayTransformBack() {
        Permanent mist = addTransformedMist();
        mist.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(mist.isTransformed()).isFalse();
        assertThat(mist.getCard().getName()).isEqualTo("Elusive Tormentor");
    }

    @Test
    @DisplayName("Declining Insidious Mist's attack payment keeps it transformed")
    void decliningUnblockedAttackPaymentKeepsMistTransformed() {
        Permanent mist = addTransformedMist();
        mist.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(mist.isTransformed()).isTrue();
        assertThat(mist.getCard().getName()).isEqualTo("Insidious Mist");
    }

    @Test
    @DisplayName("Insidious Mist cannot block")
    void mistCannotBlock() {
        Permanent attacker = addReadyCreature(player1, new DrownyardExplorers());
        Permanent mist = addTransformedMist(player2);
        attacker.setAttacking(true);
        beginBlockerDeclaration();

        int mistIndex = gd.playerBattlefields.get(player2.getId()).indexOf(mist);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(mistIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Insidious Mist cannot be blocked")
    void mistCannotBeBlocked() {
        Permanent attackingMist = addTransformedMist(player1);
        Permanent blocker = addReadyCreature(player2, new DrownyardExplorers());
        attackingMist.setAttacking(true);
        beginBlockerDeclaration();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackingMistIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attackingMist);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackingMistIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Elusive Tormentor cannot activate without a card to discard")
    void cannotActivateWithEmptyHand() {
        Permanent tormentor = addReadyTormentor();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tormentor.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Elusive Tormentor cannot activate without mana")
    void cannotActivateWithoutMana() {
        Permanent tormentor = addReadyTormentor();
        harness.setHand(player1, List.of(new DrownyardExplorers()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tormentor.isTransformed()).isFalse();
        harness.assertInHand(player1, "Drownyard Explorers");
    }

    @Test
    @DisplayName("Two queued activations transform Elusive Tormentor only once")
    void queuedActivationsTransformOnlyOnce() {
        Permanent tormentor = addReadyTormentor();
        harness.setHand(player1, List.of(new DrownyardExplorers(), new DrownyardExplorers()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        assertThat(tormentor.isTransformed()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        assertThat(tormentor.isTransformed()).isTrue();
        harness.passBothPriorities();
        assertThat(tormentor.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Insidious Mist survives lethal damage from its controller's spell")
    void mistSurvivesLethalDamage() {
        Permanent mist = addTransformedMist();
        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, mist.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mist);
        assertThat(mist.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("An opponent cannot target Insidious Mist")
    void opponentCannotTargetMist() {
        Permanent mist = addTransformedMist(player2);
        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mist.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Fiery Temper");
    }

    private Permanent addReadyTormentor() {
        return addReadyCreature(player1, new ElusiveTormentor());
    }

    private Permanent addTransformedMist() {
        return addTransformedMist(player1);
    }

    private Permanent addTransformedMist(com.github.laxika.magicalvibes.model.Player player) {
        Card front = new ElusiveTormentor();
        Permanent mist = harness.addToBattlefieldAndReturn(player, front);
        mist.setCard(front.getBackFaceCard());
        mist.setTransformed(true);
        mist.setSummoningSick(false);
        return mist;
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
