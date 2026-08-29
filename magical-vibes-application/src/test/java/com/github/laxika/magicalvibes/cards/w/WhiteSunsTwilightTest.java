package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WhiteSunsTwilightTest extends BaseCardTest {

    private void castAndResolve(int x) {
        harness.setHand(player1, List.of(new WhiteSunsTwilight()));
        harness.addMana(player1, ManaColor.WHITE, x + 2);
        harness.castSorcery(player1, 0, x);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gains X life and creates X Mites that can't block")
    void gainsLifeAndCreatesMites() {
        castAndResolve(2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        List<Permanent> mites = findPermanents(player1, "Mite");
        assertThat(mites).hasSize(2).allSatisfy(mite -> {
            assertThat(mite.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(mite.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(mite.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.MITE);
            assertThat(mite.getCard().getKeywords()).contains(Keyword.TOXIC);
            assertThat(bls.canBlock(gd, mite)).isFalse();
        });
    }

    @Test
    @DisplayName("Below X=5, leaves existing creatures on the battlefield")
    void belowThresholdDoesNotDestroyCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAndResolve(3);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("At X=5, destroys other creatures and spares the new Mites")
    void atThresholdDestroysOtherCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAndResolve(5);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
        assertThat(findPermanents(player1, "Mite")).hasSize(5);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
