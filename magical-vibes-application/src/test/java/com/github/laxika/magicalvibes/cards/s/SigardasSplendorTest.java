package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigardasSplendor.class, GrizzlyBears.class, SuntailHawk.class})
class SigardasSplendorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws at upkeep when life is at least the last noted total")
    void drawsWhenLifeIsAtLeastLastNotedTotal() {
        castSigardasSplendor();
        harness.setHand(player1, List.of());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setLife(player1, 21);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Updates the noted total even when the upkeep draw condition is false")
    void updatesNoteAfterMissedDraw() {
        castSigardasSplendor();
        harness.setHand(player1, List.of());
        Card firstTopCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(firstTopCard);
        harness.setLife(player1, 19);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(firstTopCard);

        Card secondTopCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(secondTopCard);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(secondTopCard);
    }

    @Test
    @DisplayName("Gains 1 life whenever its controller casts a white spell")
    void gainsLifeWhenControllerCastsWhiteSpell() {
        castSigardasSplendor();
        int lifeBefore = gd.getLife(player1.getId());
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not gain life when its controller casts a nonwhite spell")
    void doesNotGainLifeWhenControllerCastsNonwhiteSpell() {
        castSigardasSplendor();
        int lifeBefore = gd.getLife(player1.getId());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    private void castSigardasSplendor() {
        harness.setHand(player1, List.of(new SigardasSplendor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }
}
