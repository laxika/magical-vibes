package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Instigator.class, FreshVolunteers.class})
class InstigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card forces the target player's creatures to attack this turn")
    void forcesTargetPlayersCreaturesToAttack() {
        Permanent instigator = addCreatureReady(player1, new Instigator());
        Permanent ownCreature = addCreatureReady(player1, new FreshVolunteers());
        Permanent targetCreature = addCreatureReady(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new FreshVolunteers()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(targetCreature.isMustAttackThisTurn()).isTrue();
        assertThat(ownCreature.isMustAttackThisTurn()).isFalse();
        assertThat(instigator.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Can target the controller")
    void canTargetController() {
        addCreatureReady(player1, new Instigator());
        Permanent targetCreature = addCreatureReady(player1, new FreshVolunteers());
        harness.setHand(player1, List.of(new FreshVolunteers()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(targetCreature.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutDiscardCard() {
        addCreatureReady(player1, new Instigator());
        addCreatureReady(player2, new FreshVolunteers());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires each attackable creature controlled by the target player to attack")
    void requiresAttackWhenAble() {
        addCreatureReady(player1, new Instigator());
        Permanent targetCreature = addCreatureReady(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new FreshVolunteers()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(targetCreature.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Does not require a creature that cannot attack")
    void doesNotRequireUnableCreatureToAttack() {
        addCreatureReady(player1, new Instigator());
        Permanent targetCreature = new Permanent(new FreshVolunteers());
        targetCreature.setSummoningSick(true);
        gd.playerBattlefields.get(player2.getId()).add(targetCreature);
        harness.setHand(player1, List.of(new FreshVolunteers()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of());

        assertThat(targetCreature.isAttackedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Rejects a non-player target")
    void rejectsNonPlayerTarget() {
        addCreatureReady(player1, new Instigator());
        Permanent nonPlayerTarget = addCreatureReady(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new FreshVolunteers()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, nonPlayerTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
