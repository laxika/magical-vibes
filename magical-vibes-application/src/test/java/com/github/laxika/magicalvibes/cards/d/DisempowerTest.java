package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisempowerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts target artifact on top of its owner's library")
    void putsArtifactOnTopOfLibrary() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();
        castDisempower(harness.getPermanentId(player2, "Fountain of Youth"));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertNotInGraveyard(player2, "Fountain of Youth");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Fountain of Youth");
    }

    @Test
    @DisplayName("Resolving puts target enchantment on top of its owner's library")
    void putsEnchantmentOnTopOfLibrary() {
        harness.addToBattlefield(player2, new AngelicChorus());
        castDisempower(harness.getPermanentId(player2, "Angelic Chorus"));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName())
                .isEqualTo("Angelic Chorus");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Disempower()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDisempower(UUID targetId) {
        harness.setHand(player1, List.of(new Disempower()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
    }
}
