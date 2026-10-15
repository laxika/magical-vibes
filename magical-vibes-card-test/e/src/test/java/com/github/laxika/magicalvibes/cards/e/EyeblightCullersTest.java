package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyeblightCullers.class, GrizzlyBears.class, WrathOfGod.class})
class EyeblightCullersTest extends BaseCardTest {

    @Test
    @DisplayName("When Eyeblight Cullers dies, it creates three Elf Warriors and mills three cards")
    void deathTriggerCreatesTokensAndMillsCards() {
        harness.addToBattlefield(player1, new EyeblightCullers());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elf Warrior")).isEqualTo(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears")))
                .hasSize(3);
    }
}
