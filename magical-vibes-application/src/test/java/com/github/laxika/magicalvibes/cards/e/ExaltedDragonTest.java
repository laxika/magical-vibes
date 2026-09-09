package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExaltedDragon.class, Forest.class, TrainedArmodon.class})
class ExaltedDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted Dragon can't attack without a land to sacrifice")
    void cannotAttackWithoutLandToSacrifice() {
        addCreatureReady(player1, new ExaltedDragon());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking with Exalted Dragon sacrifices a land")
    void attackingSacrificesALand() {
        addCreatureReady(player1, new ExaltedDragon());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(firstForest.getId()));

        assertThat(findPermanents(player1, "Forest")).hasSize(1);
    }

    @Test
    @DisplayName("Only lands can be chosen for Exalted Dragon's attack cost")
    void attackCostOnlyOffersLands() {
        addCreatureReady(player1, new ExaltedDragon());
        Permanent creature = addCreatureReady(player1, new TrainedArmodon());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(player1, List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(firstForest.getId(), secondForest.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(firstForest.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Exalted Dragon")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new ExaltedDragon());
        harness.addToBattlefield(player1, new Forest());
        addCreatureReady(player2, new TrainedArmodon());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
