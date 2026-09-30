package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EssenceWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CitanulWoodreaders.class, EssenceWarden.class})
class CitanulWoodreadersTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, entering the battlefield does not draw cards")
    void withoutKickerDoesNotDraw() {
        harness.setLibrary(player1, List.of(new EssenceWarden(), new EssenceWarden()));
        harness.castFromHand(player1, new CitanulWoodreaders(), "{2}{G}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With kicker, entering the battlefield draws two cards")
    void withKickerDrawsTwoCards() {
        harness.setHand(player1, List.of(new CitanulWoodreaders()));
        harness.setLibrary(player1, List.of(new EssenceWarden(), new EssenceWarden()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
