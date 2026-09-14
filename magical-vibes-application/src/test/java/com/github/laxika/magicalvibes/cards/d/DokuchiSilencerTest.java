package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({DokuchiSilencer.class, GrizzlyBears.class, JaceBeleren.class, Mountain.class})
class DokuchiSilencerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may discard a creature card to destroy a creature or planeswalker the damaged player controls")
    void discardsCreatureToDestroyCreatureOrPlaneswalker() {
        Permanent silencer = addCreatureReady(player1, new DokuchiSilencer());
        silencer.setAttacking(true);
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent enemyPlaneswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        enemyPlaneswalker.setCounterCount(CounterType.LOYALTY, 3);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice discardChoice = gd.interaction.activeInteraction(
                PendingInteraction.DiscardChoice.class);
        assertThat(discardChoice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice targetChoice = gd.interaction.activeInteraction(
                PendingInteraction.MultiPermanentChoice.class);
        assertThat(targetChoice.validIds())
                .contains(enemyCreature.getId(), enemyPlaneswalker.getId())
                .doesNotContain(ownCreature.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(enemyPlaneswalker.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Jace Beleren");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the optional discard does not destroy anything")
    void decliningDiscardDoesNothing() {
        Permanent silencer = addCreatureReady(player1, new DokuchiSilencer());
        silencer.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability does not offer a discard when the hand has no creature card")
    void noCreatureCardMeansNoFollowUp() {
        Permanent silencer = addCreatureReady(player1, new DokuchiSilencer());
        silencer.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mountain()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("Ninjutsu returns an unblocked attacker and puts Dokuchi Silencer onto the battlefield attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DokuchiSilencer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent silencer = findPermanent(player1, "Dokuchi Silencer");
        assertThat(silencer.isTapped()).isTrue();
        assertThat(silencer.isAttacking()).isTrue();
        assertThat(silencer.getAttackTarget()).isEqualTo(player2.getId());
    }
}
