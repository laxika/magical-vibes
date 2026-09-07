package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunderingStroke.class, AirElemental.class, GrizzlyBears.class, HillGiant.class})
class SunderingStrokeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 7 damage divided among one to three targets when fewer than seven red mana was spent")
    void dividesDamageWhenFewerThanSevenRedManaWasSpent() {
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new SunderingStroke()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, Map.of(
                bears.getId(), 2,
                giant.getId(), 3,
                player2.getId(), 2));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Deals 7 damage to each target when at least seven red mana was spent")
    void dealsFullDamageToEachTargetWhenSevenRedManaWasSpent() {
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new SunderingStroke()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, Map.of(
                bears.getId(), 1,
                elemental.getId(), 2,
                player2.getId(), 4));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 7);
    }
}
