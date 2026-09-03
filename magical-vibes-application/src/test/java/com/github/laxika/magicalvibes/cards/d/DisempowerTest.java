package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Chaosphere;
import com.github.laxika.magicalvibes.cards.c.CursedTotem;
import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Disempower.class, CursedTotem.class, Chaosphere.class, FemerefScouts.class})
class DisempowerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts target artifact on top of its owner's library")
    void putsArtifactOnTopOfLibrary() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CursedTotem());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();
        castAndResolveDisempower(artifact.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(artifact.getCard());
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst()).isSameAs(artifact.getCard());
    }

    @Test
    @DisplayName("Resolving puts target enchantment on top of its owner's library")
    void putsEnchantmentOnTopOfLibrary() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Chaosphere());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();
        castAndResolveDisempower(enchantment.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantment);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(enchantment.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(enchantment.getCard());
    }

    @Test
    @DisplayName("Can target an artifact it controls")
    void canTargetOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CursedTotem());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        castAndResolveDisempower(artifact.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(artifact.getCard());
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FemerefScouts());
        prepareDisempower();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareDisempower() {
        harness.setHand(player1, List.of(new Disempower()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castAndResolveDisempower(UUID targetId) {
        prepareDisempower();
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
