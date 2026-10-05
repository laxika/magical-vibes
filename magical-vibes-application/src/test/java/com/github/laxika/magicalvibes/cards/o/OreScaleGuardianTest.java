package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.l.LavaDart;
import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OreScaleGuardian.class, SnowCoveredForest.class, LavaDart.class, MotherBear.class})
class OreScaleGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for its full cost with no land cards in the controller's graveyard")
    void canBeCastForFullCost() {
        harness.setHand(player1, List.of(new OreScaleGuardian()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs one less for each land card in the controller's graveyard")
    void costIsReducedForLandCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new SnowCoveredForest(), new SnowCoveredForest()));
        harness.setHand(player1, List.of(new OreScaleGuardian()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Nonland cards and lands in an opponent's graveyard do not reduce the cost")
    void ignoresNonlandAndOpponentGraveyardCards() {
        harness.setGraveyard(player1, List.of(new LavaDart()));
        harness.setGraveyard(player2, List.of(new SnowCoveredForest(), new SnowCoveredForest()));
        harness.setHand(player1, List.of(new OreScaleGuardian()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The reduction cannot reduce the colored mana requirement")
    void reductionCannotReduceColoredManaRequirement() {
        harness.setGraveyard(player1, List.of(
                new SnowCoveredForest(), new SnowCoveredForest(), new SnowCoveredForest(), new SnowCoveredForest(), new SnowCoveredForest(),
                new SnowCoveredForest(), new SnowCoveredForest(), new SnowCoveredForest(), new SnowCoveredForest(), new SnowCoveredForest()));
        harness.setHand(player1, List.of(new OreScaleGuardian()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Excess land cards cannot replace either required red mana")
    void excessLandCardsCannotReplaceRedMana() {
        harness.setGraveyard(player1, List.of(
                new SnowCoveredForest(), new SnowCoveredForest(), new SnowCoveredForest(),
                new SnowCoveredForest(), new SnowCoveredForest(), new SnowCoveredForest()));
        harness.setHand(player1, List.of(new OreScaleGuardian()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Haste allows the Guardian to attack the turn it is cast")
    void canAttackTheTurnItIsCast() {
        OreScaleGuardian card = new OreScaleGuardian();
        harness.castFromHand(player1, card, "{5}{R}{R}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        Permanent guardian = findPermanent(player1, "Ore-Scale Guardian");
        assertThat(guardian.getOriginalCard()).isSameAs(card);
        assertThat(guardian.isTapped()).isTrue();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking the Guardian")
    void groundCreatureCannotBlock() {
        Permanent guardian = addCreatureReady(player1, new OreScaleGuardian());
        addCreatureReady(player2, new MotherBear());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(guardian.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Another flying creature can block the Guardian")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new OreScaleGuardian());
        Permanent blocker = addCreatureReady(player2, new OreScaleGuardian());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
