package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
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




@CardUsed({SythisHarvestsHand.class, EnchantresssPresence.class, GrizzlyBears.class,
        Forest.class, GloriousAnthem.class, Counterspell.class, SwordsToPlowshares.class})
class SythisHarvestsHandTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life and draws a card when you cast an enchantment")
    void enchantmentCastGainsLifeAndDrawsCard() {
        harness.addToBattlefield(player1, new SythisHarvestsHand());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new EnchantresssPresence(), "{G}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Does not trigger for a non-enchantment spell")
    void nonEnchantmentCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new SythisHarvestsHand());
        Forest notDrawn = new Forest();
        harness.setLibrary(player1, List.of(notDrawn));
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new GrizzlyBears(), "{G}{G}");
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
        Forest notDrawn = new Forest();
        harness.setLibrary(player1, List.of(notDrawn));
        harness.setLife(player1, 10);
        harness.castFromHand(player2, new EnchantresssPresence(), "{G}{G}{G}");
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
        harness.castFromHand(player1, new GloriousAnthem(), "{W}{W}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void castingANonEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new SythisHarvestsHand());
        harness.castFromHand(player1, new GrizzlyBears(), "{G}{G}");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotTriggerForItsOwnCastOrEntry() {
        SythisHarvestsHand sythis = new SythisHarvestsHand();
        Forest notDrawn = new Forest();
        harness.setHand(player1, List.of(sythis));
        harness.setLibrary(player1, List.of(notDrawn));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sythis, Harvest's Hand");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(notDrawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enchantmentCreatureTriggersBeforeTheSpellResolves() {
        harness.addToBattlefield(player1, new SythisHarvestsHand());
        SythisHarvestsHand spell = new SythisHarvestsHand();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
    }

    @Test
    void triggerResolvesEvenWhenTheEnchantmentIsCountered() {
        harness.addToBattlefield(player1, new SythisHarvestsHand());
        EnchantresssPresence spell = new EnchantresssPresence();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Enchantress's Presence");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerResolvesAfterSythisIsExiled() {
        var sythis = harness.addToBattlefieldAndReturn(player1, new SythisHarvestsHand());
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(new EnchantresssPresence()));
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0);
        harness.castInstant(player2, 0, sythis.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sythis, Harvest's Hand");
        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).hasSize(1);
    }

}
