package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SythisHarvestsHand.class, GloriousAnthem.class, GrizzlyBears.class})
class SythisHarvestsHandTest extends BaseCardTest {

    @Test
    void castingAnEnchantmentGainsLifeAndDrawsACard() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new SythisHarvestsHand());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void castingANonEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new SythisHarvestsHand());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);
    }
}
