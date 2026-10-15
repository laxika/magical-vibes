package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StarfieldShepherd;
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

@CardUsed({LashwhipPredator.class, Forest.class, StarfieldShepherd.class})
class LashwhipPredatorTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast Lashwhip Predator for the reduced cost when an opponent controls three creatures")
    void canCastAtOpponentCreatureThreshold() {
        harness.setHand(player1, List.of(new LashwhipPredator()));
        addOpponentCreatures(3);
        addReducedCostMana();

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot cast Lashwhip Predator for the reduced cost when an opponent controls only two creatures")
    void cannotCastBelowOpponentCreatureThreshold() {
        harness.setHand(player1, List.of(new LashwhipPredator()));
        addOpponentCreatures(2);
        addReducedCostMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Your own creatures do not count toward the cost reduction")
    void ownCreaturesDoNotEnableReduction() {
        harness.setHand(player1, List.of(new LashwhipPredator()));
        addOwnCreatures(3);
        addReducedCostMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Pays full cost when the opponent has no creatures")
    void paysFullCostWithoutOpponentCreatures() {
        harness.setHand(player1, List.of(new LashwhipPredator()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lashwhip Predator");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("More than three opposing creatures still reduce the cost by only two")
    void reductionDoesNotScaleWithCreatureCount() {
        harness.setHand(player1, List.of(new LashwhipPredator()));
        addOpponentCreatures(5);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cost reduction does not remove either green mana requirement")
    void stillRequiresTwoGreenMana() {
        harness.setHand(player1, List.of(new LashwhipPredator()));
        addOpponentCreatures(3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opposing noncreature permanents do not count toward the reduction")
    void landsDoNotEnableReduction() {
        harness.setHand(player1, List.of(new LashwhipPredator()));
        addOpponentCreatures(2);
        harness.addToBattlefield(player2, new Forest());
        addReducedCostMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Reach allows Lashwhip Predator to block a flying creature")
    void canBlockFlyingCreature() {
        addCreatureReady(player1, new StarfieldShepherd());
        Permanent predator = addCreatureReady(player2, new LashwhipPredator());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(predator.isBlocking()).isTrue();
    }

    private void addOpponentCreatures(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player2, new LashwhipPredator());
        }
    }

    private void addOwnCreatures(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new LashwhipPredator());
        }
    }

    private void addReducedCostMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
