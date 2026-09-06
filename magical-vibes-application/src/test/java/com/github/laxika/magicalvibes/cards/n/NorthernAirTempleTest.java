package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NorthernAirTemple.class})
class NorthernAirTempleTest extends BaseCardTest {

    @Test
    @DisplayName("Its entry drains each opponent for the number of Shrines you control")
    void entryDrainsBasedOnControlledShrines() {
        harness.addToBattlefield(player1, shrine());
        harness.addToBattlefield(player1, shrine());
        harness.setHand(player1, List.of(new NorthernAirTemple()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Drains each opponent when another Shrine enters under your control")
    void anotherShrineEnteringDrainsEachOpponent() {
        harness.addToBattlefield(player1, new NorthernAirTemple());

        harness.enterBattlefieldAndReturn(player1, shrine());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger for a non-Shrine enchantment")
    void nonShrineEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new NorthernAirTemple());

        harness.enterBattlefieldAndReturn(player1, enchantment());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private Card shrine() {
        Card card = new Card();
        card.setName("Test Shrine");
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.SHRINE));
        return card;
    }

    private Card enchantment() {
        Card card = new Card();
        card.setName("Test Enchantment");
        card.setType(CardType.ENCHANTMENT);
        return card;
    }
}
