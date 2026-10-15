package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.b.BladesOfVelisVel;
import com.github.laxika.magicalvibes.cards.b.BlindSpotGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StinkdrinkerDaredevil.class, HillGiant.class, GrizzlyBears.class,
        BlindSpotGiant.class, AvianChangeling.class, BladesOfVelisVel.class})
class StinkdrinkerDaredevilTest extends BaseCardTest {

    @Test
    @DisplayName("Giant spells cost {2} less to cast with Stinkdrinker Daredevil on the battlefield")
    void giantSpellsCostTwoLess() {
        harness.addToBattlefield(player1, new StinkdrinkerDaredevil());
        // Hill Giant costs {3}{R} — with {2} reduction it should cost {1}{R}
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Hill Giant");
    }

    @Test
    @DisplayName("Cannot cast Giant spell without enough mana even with cost reduction")
    void cannotCastGiantWithoutEnoughMana() {
        harness.addToBattlefield(player1, new StinkdrinkerDaredevil());
        // Hill Giant costs {3}{R} — with {2} reduction needs {1}{R}; only {R} is not enough
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-Giant creature spells are not reduced")
    void nonGiantSpellsNotReduced() {
        harness.addToBattlefield(player1, new StinkdrinkerDaredevil());
        // Grizzly Bears costs {1}{G} — should not be reduced
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Only {G} is not enough for {1}{G}
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cost reduction does not apply to opponent's Giant spells")
    void doesNotReduceOpponentGiantCosts() {
        harness.addToBattlefield(player1, new StinkdrinkerDaredevil());
        // Opponent's Hill Giant should still cost {3}{R}
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 3);

        // Only {R}{R}{R} is not enough for {3}{R} — reduction does not apply to opponent
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleDaredevilsStackWithoutReducingColoredMana() {
        harness.addToBattlefield(player1, new StinkdrinkerDaredevil());
        harness.addToBattlefield(player1, new StinkdrinkerDaredevil());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reductionCannotPayColoredMana() {
        harness.addToBattlefield(player1, new StinkdrinkerDaredevil());
        harness.addToBattlefield(player1, new StinkdrinkerDaredevil());
        harness.setHand(player1, List.of(new BlindSpotGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void changelingCreatureIsReduced() {
        harness.addToBattlefield(player1, new StinkdrinkerDaredevil());
        harness.setHand(player1, List.of(new AvianChangeling()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void kindredChangelingInstantIsReduced() {
        harness.addToBattlefield(player1, new StinkdrinkerDaredevil());
        harness.setHand(player1, List.of(new BladesOfVelisVel()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reductionStopsWhenDaredevilLeavesBattlefield() {
        harness.addToBattlefield(player1, new StinkdrinkerDaredevil());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(new StinkdrinkerDaredevil()));
        harness.setHand(player1, List.of(new BlindSpotGiant()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
