package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Prismite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysticForge.class, GrizzlyBears.class, Ornithopter.class, Prismite.class, Island.class})
class MysticForgeTest extends BaseCardTest {

    @Test
    @DisplayName("casts an artifact spell from the top of the library")
    void castsArtifactSpellFromTopOfLibrary() {
        harness.addToBattlefield(player1, new MysticForge());
        Card artifact = new Ornithopter();
        gd.playerDecks.get(player1.getId()).addFirst(artifact);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("casts a colorless nonartifact spell from the top of the library")
    void castsColorlessNonartifactSpellFromTopOfLibrary() {
        harness.addToBattlefield(player1, new MysticForge());
        Card colorlessSpell = new Card();
        colorlessSpell.setName("Colorless Spell");
        colorlessSpell.setType(CardType.INSTANT);
        colorlessSpell.setManaCost("{1}");
        gd.playerDecks.get(player1.getId()).addFirst(colorlessSpell);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(colorlessSpell);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(colorlessSpell);
    }

    @Test
    @DisplayName("does not cast a colored nonartifact spell from the top of the library")
    void rejectsColoredNonartifactSpellFromTopOfLibrary() {
        harness.addToBattlefield(player1, new MysticForge());
        Card coloredSpell = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(coloredSpell);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(coloredSpell);
    }

    @Test
    @DisplayName("tapping and paying 1 life exiles the top card")
    void tapsPaysLifeAndExilesTopCard() {
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new MysticForge());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(forge.isTapped()).isTrue();
        harness.assertLife(player1, 19);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void looksAtUncastableTopCardPrivatelyWithoutPriority() {
        harness.addToBattlefield(player1, new MysticForge());
        harness.setLibrary(player1, List.of(new Island()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Island") && message.contains("}],[]]"));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void cannotPlayLandFromLibraryTop() {
        harness.addToBattlefield(player1, new MysticForge());
        Card land = new Island();
        harness.setLibrary(player1, List.of(land));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void requiresManaForArtifactSpell() {
        harness.addToBattlefield(player1, new MysticForge());
        Card artifact = new Prismite();
        harness.setLibrary(player1, List.of(artifact));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
    }

    @Test
    void canCastSuccessiveArtifactsInSameTurn() {
        harness.addToBattlefield(player1, new MysticForge());
        Card first = new Prismite();
        Card second = new Prismite();
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveFromLibraryTop(player1);
        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotGrantArtifactSpellsFlash() {
        harness.addToBattlefield(player1, new MysticForge());
        Card artifact = new Prismite();
        harness.setLibrary(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
    }

    @Test
    void paysCostsImmediatelyAndExilesCurrentTopCardOnResolution() {
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new MysticForge());
        Card first = new Prismite();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, null, null);

        assertThat(forge.isTapped()).isTrue();
        harness.assertLife(player1, 19);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(first, second);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Prismite");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(second).doesNotContain(first);
        harness.assertLife(player1, 19);
    }

    @Test
    void canActivateWithEmptyLibrary() {
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new MysticForge());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(forge.isTapped()).isTrue();
        harness.assertLife(player1, 19);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
