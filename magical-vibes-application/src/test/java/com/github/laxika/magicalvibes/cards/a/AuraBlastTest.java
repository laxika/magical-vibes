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
        harness.castAndResolveInstant(player1, 0, targetId);

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

    @Test
    @DisplayName("Can destroy its controller's enchantment and draw a card")
    void destroysOwnEnchantmentAndDrawsCard() {
        var target = harness.addToBattlefieldAndReturn(player1, new CloudCover());
        harness.setHand(player1, List.of(new AuraBlast()));
        harness.setLibrary(player1, List.of(new QuirionExplorer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Cloud Cover");
        harness.assertInGraveyard(player1, "Cloud Cover");
        harness.assertInGraveyard(player1, "Aura Blast");
        harness.assertInHand(player1, "Quirion Explorer");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when its only target leaves before resolution")
    void doesNotDrawWhenTargetLeavesBeforeResolution() {
        var target = harness.addToBattlefieldAndReturn(player2, new CloudCover());
        harness.setHand(player1, List.of(new AuraBlast()));
        harness.setHand(player2, List.of(new AuraBlast()));
        harness.setLibrary(player1, List.of(new QuirionExplorer()));
        harness.setLibrary(player2, List.of(new QuirionExplorer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertInGraveyard(player2, "Cloud Cover");
        harness.assertInHand(player2, "Quirion Explorer");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aura Blast");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
