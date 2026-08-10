package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrashForTreasureTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices an artifact and returns a target artifact card to the battlefield")
    void sacrificesArtifactAndReturnsArtifact() {
        Card artifactCard = new Ornithopter();
        Permanent sacrificedArtifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player1.getId()).add(sacrificedArtifact);
        harness.setGraveyard(player1, List.of(artifactCard));
        harness.setHand(player1, List.of(new TrashForTreasure()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, artifactCard.getId(), sacrificedArtifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Trash for Treasure");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(artifactCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(artifactCard.getId()));
    }

    @Test
    @DisplayName("Cannot target a non-artifact card in a graveyard")
    void cannotTargetNonArtifactCard() {
        Card nonArtifactCard = new HolyDay();
        Permanent sacrificedArtifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player1.getId()).add(sacrificedArtifact);
        harness.setGraveyard(player1, List.of(nonArtifactCard));
        harness.setHand(player1, List.of(new TrashForTreasure()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, nonArtifactCard.getId(), sacrificedArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast without an artifact to sacrifice")
    void cannotCastWithoutArtifact() {
        Card artifactCard = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifactCard));
        harness.setHand(player1, List.of(new TrashForTreasure()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, artifactCard.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot target an artifact card in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        Card artifactCard = new Ornithopter();
        Permanent sacrificedArtifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player1.getId()).add(sacrificedArtifact);
        harness.setGraveyard(player2, List.of(artifactCard));
        harness.setHand(player1, List.of(new TrashForTreasure()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, artifactCard.getId(), sacrificedArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }
}
