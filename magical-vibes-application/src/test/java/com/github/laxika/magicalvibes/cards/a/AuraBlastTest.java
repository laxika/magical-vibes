package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CloudCover;
import com.github.laxika.magicalvibes.cards.q.QuirionExplorer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuraBlast.class, CloudCover.class, QuirionExplorer.class})
class AuraBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target enchantment and draws a card")
    void destroysEnchantmentAndDrawsCard() {
        harness.addToBattlefield(player2, new CloudCover());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new AuraBlast()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Cloud Cover");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cloud Cover");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Cannot target a non-enchantment permanent")
    void cannotTargetNonEnchantment() {
        harness.addToBattlefield(player2, new QuirionExplorer());
        harness.setHand(player1, List.of(new AuraBlast()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Quirion Explorer");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchantment");
    }
}
