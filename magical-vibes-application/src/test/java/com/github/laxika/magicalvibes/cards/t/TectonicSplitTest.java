package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TectonicSplit.class, Forest.class})
class TectonicSplitTest extends BaseCardTest {

    @Test
    void sacrificesHalfOfControlledLandsRoundedUp() {
        harness.setHand(player1, List.of(new TectonicSplit()));
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorceryWithSacrifices(player1, 0, null, List.of(firstLand.getId(), secondLand.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest")))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Forest")))
                .hasSize(2);
        harness.assertOnBattlefield(player1, "Tectonic Split");
    }

    @Test
    void controlledLandsCanTapForThreeManaOfAnyColor() {
        harness.addToBattlefield(player1, new TectonicSplit());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setSummoningSick(false);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(forest.isTapped()).isTrue();
    }
}
