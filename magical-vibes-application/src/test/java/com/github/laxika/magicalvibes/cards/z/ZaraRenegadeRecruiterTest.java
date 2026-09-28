package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZaraRenegadeRecruiter.class, GrizzlyBears.class, Forest.class})
class ZaraRenegadeRecruiterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking may put a creature from the defending player's hand onto the battlefield tapped and attacking")
    void attackingPutsDefendingCreatureTappedAndAttacking() {
        addZaraReady();
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());
        harness.setHand(player2, List.of(creature));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.TargetedHandBattlefieldChoice.class);
        assertThat(((PendingInteraction.TargetedHandBattlefieldChoice) gd.interaction.activeInteraction()).attackTargetId())
                .isEqualTo(player2.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> harness.handleCardChosen(player1, 0));

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.isAttacking()).isTrue();
        assertThat(bears.isAttackedThisTurn()).isTrue();
        assertThat(bears.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The creature returns to its owner's hand at the next end step")
    void attackingCreatureReturnsAtNextEndStep() {
        addZaraReady();
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());
        harness.setHand(player2, List.of(creature));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the defending player's hand unchanged")
    void decliningLeavesDefendingHandUnchanged() {
        addZaraReady();
        harness.setHand(player2, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not choose a noncreature card from the defending player's hand")
    void doesNotChooseNoncreatureCard() {
        addZaraReady();
        harness.setHand(player2, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player2, "Forest");
    }

    private void addZaraReady() {
        addCreatureReady(player1, new ZaraRenegadeRecruiter());
    }
}
