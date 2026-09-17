package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.o.OverwhelmingInstinct;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuraExtraction.class, OverwhelmingInstinct.class, GlorySeeker.class})
class AuraExtractionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts target enchantment on top of its owner's library")
    void putsTargetEnchantmentOnTopOfOwnersLibrary() {
        harness.addToBattlefield(player2, new OverwhelmingInstinct());
        UUID targetId = harness.getPermanentId(player2, "Overwhelming Instinct");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new AuraExtraction()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Overwhelming Instinct");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName())
                .isEqualTo("Overwhelming Instinct");
        harness.assertInGraveyard(player1, "Aura Extraction");
    }

    @Test
    @DisplayName("Puts a controlled enchantment into its owner's library")
    void putsControlledEnchantmentIntoOwnersLibrary() {
        OverwhelmingInstinct target = new OverwhelmingInstinct();
        target.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, target);
        UUID targetId = harness.getPermanentId(player2, "Overwhelming Instinct");
        int ownerDeckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        int controllerDeckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new AuraExtraction()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Overwhelming Instinct");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(ownerDeckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName())
                .isEqualTo("Overwhelming Instinct");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(controllerDeckSizeBefore);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new AuraExtraction()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Glory Seeker");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDiscardsAndDraws() {
        harness.setHand(player1, List.of(new AuraExtraction()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aura Extraction");
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Cycling requires two mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new AuraExtraction()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Aura Extraction");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }
}
