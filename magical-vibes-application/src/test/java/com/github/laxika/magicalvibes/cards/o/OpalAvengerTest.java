package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpalAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 3/5 Soldier creature when its controller has 10 or less life")
    void becomesCreatureWhenControllerHasTenOrLessLife() {
        harness.addToBattlefield(player1, new OpalAvenger());
        Permanent avenger = findPermanent(player1, "Opal Avenger");

        harness.setLife(player1, 10);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, avenger)).isTrue();
        assertThat(gqs.isEnchantment(gd, avenger)).isFalse();
        assertThat(avenger.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(avenger.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
        assertThat(avenger.getCard().getPower()).isEqualTo(3);
        assertThat(avenger.getCard().getToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not transform above the life threshold and does not revert after transforming")
    void thresholdAndPermanentTransformation() {
        harness.addToBattlefield(player1, new OpalAvenger());
        Permanent avenger = findPermanent(player1, "Opal Avenger");

        harness.setLife(player1, 11);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, avenger)).isFalse();
        assertThat(gqs.isEnchantment(gd, avenger)).isTrue();

        harness.setLife(player1, 10);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.setLife(player1, 11);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, avenger)).isTrue();
        assertThat(gqs.isEnchantment(gd, avenger)).isFalse();
    }

    @Test
    @DisplayName("Checks its controller's life total, not an opponent's")
    void checksControllerLife() {
        harness.addToBattlefield(player1, new OpalAvenger());
        Permanent avenger = findPermanent(player1, "Opal Avenger");

        harness.setLife(player2, 10);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, avenger)).isFalse();
        assertThat(gqs.isEnchantment(gd, avenger)).isTrue();
    }
}
