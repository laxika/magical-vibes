package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AssassinInitiate;
import com.github.laxika.magicalvibes.cards.g.GiantOctopus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LydiaFrye.class, AssassinInitiate.class, GiantOctopus.class, GrizzlyBears.class})
class LydiaFryeTest extends BaseCardTest {

    @Test
    @DisplayName("Lydia Frye can't be blocked by creatures with power 3 or greater")
    void cannotBeBlockedByPowerThreeOrGreaterCreature() {
        Permanent blocker = addCreatureReady(player2, new GiantOctopus());
        Permanent lydia = addCreatureReady(player1, new LydiaFrye());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
        assertThat(lydia.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Lydia Frye can be blocked by a creature with power less than 3")
    void canBeBlockedByCreatureWithPowerLessThanThree() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new LydiaFrye());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The end-step trigger surveils for each tapped Assassin you control")
    void surveilsForTappedAssassins() {
        addCreatureReady(player1, new LydiaFrye());
        Permanent tappedAssassin = addCreatureReady(player1, new AssassinInitiate());
        addCreatureReady(player1, new AssassinInitiate());
        Card first = new GrizzlyBears();
        Card second = new GiantOctopus();
        harness.setLibrary(player1, List.of(first, second));

        tappedAssassin.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(tappedAssassin.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }
}
