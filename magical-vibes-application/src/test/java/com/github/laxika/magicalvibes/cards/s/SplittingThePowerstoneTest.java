package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SplittingThePowerstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two tapped Powerstones and draws when the sacrificed artifact is legendary")
    void legendaryArtifactCreatesPowerstonesAndDraws() {
        com.github.laxika.magicalvibes.cards.s.Spellbook legendaryArtifact = new Spellbook();
        legendaryArtifact.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, legendaryArtifact);
        harness.setLibrary(player1, List.of(new com.github.laxika.magicalvibes.cards.g.GrizzlyBears()));
        harness.setHand(player1, List.of(new SplittingThePowerstone()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorceryWithSacrifice(player1, 0, artifact.getId());
        harness.passBothPriorities();

        List<Permanent> powerstones = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.POWERSTONE))
                .toList();
        assertThat(powerstones).hasSize(2);
        assertThat(powerstones).allMatch(permanent -> permanent.isTapped()
                && permanent.getCard().hasType(CardType.ARTIFACT));
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw when the sacrificed artifact is not legendary")
    void nonlegendaryArtifactDoesNotDraw() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setLibrary(player1, List.of(new com.github.laxika.magicalvibes.cards.g.GrizzlyBears()));
        harness.setHand(player1, List.of(new SplittingThePowerstone()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorceryWithSacrifice(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card instanceof com.github.laxika.magicalvibes.cards.g.GrizzlyBears);
    }

    @Test
    @DisplayName("Cannot cast without an artifact to sacrifice")
    void cannotCastWithoutArtifact() {
        harness.setHand(player1, List.of(new SplittingThePowerstone()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }
}
