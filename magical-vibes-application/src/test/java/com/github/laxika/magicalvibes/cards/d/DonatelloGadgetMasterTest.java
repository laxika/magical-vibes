package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
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

@CardUsed({DonatelloGadgetMaster.class, GrizzlyBears.class, HowlingMine.class})
class DonatelloGadgetMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage creates a token copy of a targeted artifact you control")
    void createsTokenCopyOfTargetArtifact() {
        Permanent donatello = addCreatureReady(player1, new DonatelloGadgetMaster());
        donatello.setAttacking(true);
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new HowlingMine());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(mine.getId());
        harness.handlePermanentChosen(player1, mine.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Howling Mine")).hasSize(2);
        assertThat(findPermanents(player1, "Howling Mine")).filteredOn(p -> p.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("The combat trigger only permits artifacts controlled by Donatello's controller")
    void onlyOwnArtifactsAreValidTargets() {
        Permanent donatello = addCreatureReady(player1, new DonatelloGadgetMaster());
        donatello.setAttacking(true);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new HowlingMine());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(ownArtifact.getId())
                .doesNotContain(ownCreature.getId(), opponentArtifact.getId());
    }

    @Test
    @DisplayName("No combat trigger is put on the stack when there is no artifact to target")
    void doesNotTriggerWithoutOwnArtifact() {
        Permanent donatello = addCreatureReady(player1, new DonatelloGadgetMaster());
        donatello.setAttacking(true);
        harness.addToBattlefield(player2, new HowlingMine());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and puts Donatello in tapped and attacking")
    void sneakSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        harness.setHand(player1, List.of(new DonatelloGadgetMaster()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent donatello = findPermanent(player1, "Donatello, Gadget Master");
        assertThat(donatello.isTapped()).isTrue();
        assertThat(donatello.isAttacking()).isTrue();
        assertThat(donatello.getAttackTarget()).isEqualTo(player2.getId());

        harness.handlePermanentChosen(player1, mine.getId());
        harness.passBothPriorities();
    }
}
