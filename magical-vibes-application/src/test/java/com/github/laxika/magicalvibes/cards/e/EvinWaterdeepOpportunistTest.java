package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Treasure;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EvinWaterdeepOpportunist.class, DiabolicEdict.class, GrizzlyBears.class, Treasure.class})
class EvinWaterdeepOpportunistTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 for each Treasure controlled")
    void boostsForTreasuresControlled() {
        harness.addToBattlefield(player1, new EvinWaterdeepOpportunist());
        harness.addToBattlefield(player1, new Treasure());
        harness.addToBattlefield(player1, new Treasure());
        harness.addToBattlefield(player2, new Treasure());

        Permanent evin = findPermanent(player1, "Evin, Waterdeep Opportunist");
        assertThat(gqs.getEffectivePower(gd, evin)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, evin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creates one Treasure after a creature is sacrificed and does not trigger again that turn")
    void createsTreasureOnceEachTurn() {
        harness.addToBattlefield(player1, new EvinWaterdeepOpportunist());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castEdictAt(player2);

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        harness.addToBattlefield(player2, new GrizzlyBears());
        castEdictAt(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private void castEdictAt(com.github.laxika.magicalvibes.model.Player target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
    }
}
