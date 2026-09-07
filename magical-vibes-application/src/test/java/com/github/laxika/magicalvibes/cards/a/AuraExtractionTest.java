package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuraExtraction.class, AngelicChorus.class, GrizzlyBears.class})
class AuraExtractionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts target enchantment on top of its owner's library")
    void putsTargetEnchantmentOnTopOfOwnersLibrary() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new AuraExtraction()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        assertThat(gameData.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gameData.playerDecks.get(player2.getId()).getFirst().getName())
                .isEqualTo("Angelic Chorus");
        harness.assertInGraveyard(player1, "Aura Extraction");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AuraExtraction()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDiscardsAndDraws() {
        harness.setHand(player1, List.of(new AuraExtraction()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aura Extraction");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
