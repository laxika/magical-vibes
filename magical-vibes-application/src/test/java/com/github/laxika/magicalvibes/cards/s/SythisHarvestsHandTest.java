package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EnchantresssPresence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;




@CardUsed({SythisHarvestsHand.class, EnchantresssPresence.class, GrizzlyBears.class, Forest.class, GloriousAnthem.class})
class SythisHarvestsHandTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life and draws a card when you cast an enchantment")
    void enchantmentCastGainsLifeAndDrawsCard() {
        harness.addToBattlefield(player1, new SythisHarvestsHand());
        harness.setHand(player1, List.of(new EnchantresssPresence()));
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Does not trigger for a non-enchantment spell")
    void nonEnchantmentCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new SythisHarvestsHand());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        Forest notDrawn = new Forest();
        harness.setLibrary(player1, List.of(notDrawn));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(notDrawn);
        assertThat(gd.playerDecks.get(player1.getId())).contains(notDrawn);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's enchantment spell")
    void opponentEnchantmentCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new SythisHarvestsHand());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new EnchantresssPresence()));
        Forest notDrawn = new Forest();
        harness.setLibrary(player1, List.of(notDrawn));
        harness.setLife(player1, 10);
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(notDrawn);
        assertThat(gd.playerDecks.get(player1.getId())).contains(notDrawn);
    }
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
