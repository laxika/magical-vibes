package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZoyowaLavaTongue.class, GrizzlyBears.class, Shock.class})
class ZoyowaLavaTongueTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger when its controller has not descended")
    void doesNotTriggerWithoutDescended() {
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent may discard instead of taking damage")
    void opponentDiscards() {
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());
        descend(player1);
        harness.setHand(player2, List.of(new Shock()));

        resolveTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent may sacrifice instead of taking damage")
    void opponentSacrifices() {
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());
        descend(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        resolveTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent who declines both options is dealt 3 damage")
    void opponentDeclinesBothOptions() {
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());
        descend(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addToBattlefield(player2, new GrizzlyBears());

        resolveTrigger();

        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent with no available option is dealt 3 damage")
    void opponentWithNoOptionTakesDamage() {
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());
        descend(player1);

        resolveTrigger();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void descend(Player player) {
        Permanent fodder = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, fodder));
    }

    private void resolveTrigger() {
        advanceToEndStep(player1);
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
