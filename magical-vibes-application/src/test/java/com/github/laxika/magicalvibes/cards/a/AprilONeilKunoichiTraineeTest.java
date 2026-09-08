package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AprilONeilKunoichiTrainee.class, GrizzlyBears.class})
class AprilONeilKunoichiTraineeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield prompts to scry 2")
    void scriesTwoOnEnter() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.add(0, new GrizzlyBears());
        deck.add(1, new GrizzlyBears());

        harness.setHand(player1, List.of(new AprilONeilKunoichiTrainee()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    @Test
    @DisplayName("Cannot be blocked by a creature with power 3")
    void cannotBeBlockedByPowerThreeCreature() {
        Card blockerCard = new GrizzlyBears();
        blockerCard.setPower(3);
        Permanent blocker = addCreatureReady(player2, blockerCard);
        Permanent april = addCreatureReady(player1, new AprilONeilKunoichiTrainee());
        april.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(april);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by a creature with power less than 3")
    void canBeBlockedByPowerLessThanThreeCreature() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent april = addCreatureReady(player1, new AprilONeilKunoichiTrainee());
        april.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(april);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
