package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArchfiendOfDepravityTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent keeps up to two creatures and sacrifices the rest")
    void opponentKeepsUpToTwoCreatures() {
        harness.addToBattlefield(player1, new ArchfiendOfDepravity());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Millstone());

        advanceToEndStep(player2);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(first, second, noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(third.getCard());
    }

    @Test
    @DisplayName("The opponent may keep one or no creatures")
    void opponentMayKeepFewerThanTwoCreatures() {
        harness.addToBattlefield(player1, new ArchfiendOfDepravity());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player2);
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(second.getCard(), third.getCard());
    }

    @Test
    @DisplayName("The opponent's choice may spare no creatures")
    void opponentMayKeepNoCreatures() {
        harness.addToBattlefield(player1, new ArchfiendOfDepravity());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player2);
        harness.handleMultiplePermanentsChosen(player2, List.of());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(first.getCard(), second.getCard());
    }

    @Test
    @DisplayName("It does not trigger on its controller's end step")
    void doesNotTriggerOnControllersEndStep() {
        harness.addToBattlefield(player1, new ArchfiendOfDepravity());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class))
                .isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
