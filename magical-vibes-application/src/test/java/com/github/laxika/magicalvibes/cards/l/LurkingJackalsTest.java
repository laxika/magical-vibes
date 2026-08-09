package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LurkingJackalsTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 3/2 Jackal creature when an opponent has 10 or less life")
    void becomesCreatureWhenOpponentHasTenOrLessLife() {
        harness.addToBattlefield(player1, new LurkingJackals());
        Permanent jackals = findPermanent(player1, "Lurking Jackals");

        harness.setLife(player2, 10);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, jackals)).isTrue();
        assertThat(gqs.isEnchantment(gd, jackals)).isFalse();
        assertThat(jackals.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(jackals.getCard().getSubtypes()).containsExactly(CardSubtype.JACKAL);
        assertThat(jackals.getCard().getPower()).isEqualTo(3);
        assertThat(jackals.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not transform above the life threshold and does not revert after transforming")
    void thresholdAndPermanentTransformation() {
        harness.addToBattlefield(player1, new LurkingJackals());
        Permanent jackals = findPermanent(player1, "Lurking Jackals");

        harness.setLife(player2, 11);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, jackals)).isFalse();
        assertThat(gqs.isEnchantment(gd, jackals)).isTrue();

        harness.setLife(player2, 10);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.setLife(player2, 11);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, jackals)).isTrue();
        assertThat(gqs.isEnchantment(gd, jackals)).isFalse();
    }
}
