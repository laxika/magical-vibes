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

    @Test
    @DisplayName("Uses the owner's library when another player controls the artifact")
    void putsArtifactInOwnersLibraryRatherThanControllers() {
        CursedTotem card = new CursedTotem();
        card.setOwnerId(player2.getId());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, card);
        List<Card> controllerLibraryBefore = List.copyOf(harness.getGameData().playerDecks.get(player1.getId()));
        List<Card> ownerLibraryBefore = List.copyOf(harness.getGameData().playerDecks.get(player2.getId()));

        castAndResolveDisempower(artifact.getId());

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactlyElementsOf(controllerLibraryBefore);
        List<Card> ownerLibrary = harness.getGameData().playerDecks.get(player2.getId());
        assertThat(ownerLibrary.getFirst()).isSameAs(card);
        assertThat(ownerLibrary.subList(1, ownerLibrary.size())).containsExactlyElementsOf(ownerLibraryBefore);
    }

    @Test
    @DisplayName("Can put an enchantment on top of an empty library")
    void putsEnchantmentIntoEmptyLibrary() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Chaosphere());
        harness.setLibrary(player2, List.of());

        castAndResolveDisempower(enchantment.getId());

        assertThat(harness.getGameData().playerDecks.get(player2.getId())).containsExactly(enchantment.getCard());
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).doesNotContain(enchantment);
    }

    @Test
    @DisplayName("Does nothing when its target leaves the battlefield before resolution")
    void doesNotMoveTargetAgainAfterItLeavesBattlefield() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CursedTotem());
        prepareDisempower();
        harness.castInstant(player1, 0, artifact.getId());
        harness.setHand(player2, List.of(new Disempower()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, artifact.getId());
        List<Card> libraryAfterResponse = List.copyOf(harness.getGameData().playerDecks.get(player2.getId()));

        harness.passBothPriorities();

        assertThat(harness.getGameData().playerDecks.get(player2.getId())).containsExactlyElementsOf(libraryAfterResponse);
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        harness.assertInGraveyard(player1, "Disempower");
        harness.assertInGraveyard(player2, "Disempower");
        assertThat(harness.getGameData().stack).isEmpty();
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
