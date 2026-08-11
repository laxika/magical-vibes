package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
}
