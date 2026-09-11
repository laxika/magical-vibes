package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LongBodiedGreyDog.class)
class LongBodiedGreyDogTest extends BaseCardTest {

    @Test
    @DisplayName("When Long-Bodied Grey Dog enters, it creates a tapped Treasure token")
    void entersCreatesTappedTreasure() {
        harness.setHand(player1, List.of(new LongBodiedGreyDog()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isTrue();
        assertThat(treasure.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        assertThat(treasure.getCard().isToken()).isTrue();
    }
}
