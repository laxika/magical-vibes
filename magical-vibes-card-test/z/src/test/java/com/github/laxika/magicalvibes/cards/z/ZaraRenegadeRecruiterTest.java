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
        assertThat(bears.isAttackedThisTurn()).isFalse();
        assertThat(bears.getAttacksThisTurn()).isZero();
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

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

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

    @Test
    @DisplayName("The whole defending hand is visible before deciding whether to recruit")
    void looksAtHandBeforeOptionalRecruitment() {
        addZaraReady();
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Forest") && message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
    }

    @Test
    @DisplayName("Declining recruitment still looks at the defending player's hand")
    void decliningStillLooksAtHand() {
        addZaraReady();
        harness.setHand(player2, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An empty defending hand finishes without a card-selection interaction")
    void emptyDefendingHandFinishesNormally() {
        addZaraReady();
        harness.setHand(player2, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.TargetedHandBattlefieldChoice.class);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing a creature from a mixed hand leaves the noncreature card behind")
    void recruitsCreatureFromMixedHand() {
        addZaraReady();
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());
        harness.setHand(player2, List.of(new Forest(), creature));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> harness.handleCardChosen(player1, 1));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Forest");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("The end-step return waits for its delayed triggered ability to resolve")
    void returnUsesStackAtBeginningOfEndStep() {
        addZaraReady();
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());
        harness.setHand(player2, List.of(creature));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> harness.handleCardChosen(player1, 0));
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    private void addZaraReady() {
        addCreatureReady(player1, new ZaraRenegadeRecruiter());
    }
}
