package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HaazdaSnareSquad.class, KraulWarrior.class})
class HaazdaSnareSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {W} taps the targeted opponent creature")
    void payingTapsTargetCreature() {
        addCreatureReady(player1, new HaazdaSnareSquad());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new KraulWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the payment leaves the target untapped")
    void decliningLeavesTargetUntapped() {
        addCreatureReady(player1, new HaazdaSnareSquad());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new KraulWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("No target selection when the opponent controls no creatures")
    void noTargetSelectionWithoutOpponentCreature() {
        addCreatureReady(player1, new HaazdaSnareSquad());
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("Own creatures are not legal targets for the attack trigger")
    void ownCreatureNotTapped() {
        addCreatureReady(player1, new HaazdaSnareSquad());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new KraulWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(0));
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(opponentBears.isTapped()).isTrue();
        assertThat(ownBears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Target selection is required even without white mana")
    void choosesTargetWithoutMana() {
        addCreatureReady(player1, new HaazdaSnareSquad());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KraulWarrior());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Payment spends one white mana during resolution")
    void paymentSpendsWhiteMana() {
        addCreatureReady(player1, new HaazdaSnareSquad());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KraulWarrior());
        harness.addMana(player1, ManaColor.WHITE, 2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(target.isTapped()).isTrue();
        resolveAllTriggers();
    }

    @Test
    @DisplayName("An already tapped opponent creature is a legal target")
    void canTargetTappedCreature() {
        addCreatureReady(player1, new HaazdaSnareSquad());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KraulWarrior());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A target leaving the battlefield prevents the payment choice")
    void missingTargetPreventsPayment() {
        addCreatureReady(player1, new HaazdaSnareSquad());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KraulWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(target.isTapped()).isFalse();
    }
}
