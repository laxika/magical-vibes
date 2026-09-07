package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BebopRocksteady.class, GrizzlyBears.class, Shock.class})
class BebopRocksteadyTest extends BaseCardTest {

    @Test
    @DisplayName("The controller can discard a card instead of sacrificing a permanent when attacking")
    void discardsInsteadOfSacrificingWhenAttacking() {
        Shock discarded = new Shock();
        harness.setHand(player1, List.of(discarded));
        Permanent bebopRocksteady = addCreatureReady(player1, new BebopRocksteady());
        Permanent otherPermanent = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Discard a card");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bebopRocksteady, otherPermanent);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("The controller can sacrifice a permanent instead of discarding when attacking")
    void sacrificesInsteadOfDiscardingWhenAttacking() {
        Shock inHand = new Shock();
        harness.setHand(player1, List.of(inHand));
        addCreatureReady(player1, new BebopRocksteady());
        Permanent otherPermanent = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Sacrifice a permanent");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, otherPermanent.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(otherPermanent.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(inHand);
    }

    @Test
    @DisplayName("The trigger also resolves when Bebop & Rocksteady blocks")
    void triggersWhenBlocking() {
        Permanent bebopRocksteady = addCreatureReady(player1, new BebopRocksteady());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherPermanent = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));

        bebopRocksteady.setAttacking(false);
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new com.github.laxika.magicalvibes.networking.message.BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(bebopRocksteady))));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Discard a card");
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bebopRocksteady, otherPermanent);
    }
}
