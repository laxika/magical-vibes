package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LuminolluskTest extends BaseCardTest {

    @Test
    void gainsOneLifeWhenItIsTheOnlyColorAmongControlledPermanents() {
        castLuminollusk();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    void gainsLifeForEachDistinctColorAmongControlledPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new RagingGoblin());

        castLuminollusk();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void ignoresColorsAmongOpponentsPermanents() {
        harness.addToBattlefield(player2, new AirElemental());

        castLuminollusk();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    private void castLuminollusk() {
        harness.setHand(player1, List.of(new Luminollusk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
