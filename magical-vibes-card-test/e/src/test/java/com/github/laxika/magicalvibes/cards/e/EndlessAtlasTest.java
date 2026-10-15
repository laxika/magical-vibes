package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EndlessAtlas.class, Forest.class, Island.class, Mountain.class})
class EndlessAtlasTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card with three lands that share a name")
    void drawsWithThreeLandsOfTheSameName() {
        Permanent atlas = harness.addToBattlefieldAndReturn(player1, new EndlessAtlas());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(atlas.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot draw with three lands that have different names")
    void requiresThreeLandsOfTheSameName() {
        Permanent atlas = harness.addToBattlefieldAndReturn(player1, new EndlessAtlas());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three or more lands with the same name");
        assertThat(atlas.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's matching lands do not satisfy the activation restriction")
    void doesNotCountOpponentsLands() {
        Permanent atlas = harness.addToBattlefieldAndReturn(player1, new EndlessAtlas());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three or more lands with the same name");
        assertThat(atlas.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Matching nonland permanents do not satisfy the activation restriction")
    void doesNotCountMatchingArtifacts() {
        Permanent atlas = harness.addToBattlefieldAndReturn(player1, new EndlessAtlas());
        harness.addToBattlefield(player1, new EndlessAtlas());
        harness.addToBattlefield(player1, new EndlessAtlas());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three or more lands with the same name");
        assertThat(atlas.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The land restriction is not checked again when the ability resolves")
    void drawsAfterLosingQualifyingLand() {
        harness.addToBattlefield(player1, new EndlessAtlas());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Mountain(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(forest);
        gd.playerGraveyards.get(player1.getId()).add(forest.getCard());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Activation requires two mana even with enough matching lands")
    void requiresTwoMana() {
        Permanent atlas = harness.addToBattlefieldAndReturn(player1, new EndlessAtlas());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(atlas.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Atlas cannot activate again")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new EndlessAtlas());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
