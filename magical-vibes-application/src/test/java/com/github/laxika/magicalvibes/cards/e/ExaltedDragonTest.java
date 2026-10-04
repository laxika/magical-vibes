package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({ExaltedDragon.class, Forest.class, TrainedArmodon.class, Humble.class})
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

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each attacking Dragon requires a separate land")
    void twoDragonsCannotShareOneLand() {
        addCreatureReady(player1, new ExaltedDragon());
        addCreatureReady(player1, new ExaltedDragon());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
    }

    @Test
    @DisplayName("Two attacking Dragons sacrifice two lands")
    void twoDragonsSacrificeTwoLands() {
        addCreatureReady(player1, new ExaltedDragon());
        addCreatureReady(player1, new ExaltedDragon());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.addToBattlefield(player1, first);
        harness.addToBattlefield(player1, second);

        declareAttackers(player1, List.of(0, 1));

        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
    }

    @Test
    @DisplayName("An opponent's land cannot pay the attack cost")
    void opponentsLandCannotPayAttackCost() {
        addCreatureReady(player1, new ExaltedDragon());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest);
    }

    @Test
    @CardUsed({ExaltedDragon.class, Humble.class})
    @DisplayName("Losing all abilities removes the land sacrifice attack cost")
    void losingAbilitiesRemovesAttackCost() {
        Permanent dragon = addCreatureReady(player1, new ExaltedDragon());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, dragon.getId());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThat(dragon.isAttacking()).isTrue();
    }
}
