package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.g.GoblinStriker;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrashForTreasure.class, AlphaMyr.class, GoblinStriker.class, LeoninScimitar.class,
        Ornithopter.class, Shatter.class})
class TrashForTreasureTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices an artifact and returns a target artifact card to the battlefield")
    void sacrificesArtifactAndReturnsArtifact() {
        Card artifactCard = new Ornithopter();
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        harness.setGraveyard(player1, List.of(artifactCard));
        harness.setHand(player1, List.of(new TrashForTreasure()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, artifactCard.getId(), sacrificedArtifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Alpha Myr");
        harness.assertInGraveyard(player1, "Alpha Myr");
        harness.assertInGraveyard(player1, "Trash for Treasure");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(artifactCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(artifactCard.getId()));
    }

    @Test
    @DisplayName("Returns a noncreature artifact card")
    void returnsNoncreatureArtifact() {
        Card artifactCard = new LeoninScimitar();
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        harness.setGraveyard(player1, List.of(artifactCard));
        harness.setHand(player1, List.of(new TrashForTreasure()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, artifactCard.getId(), sacrificedArtifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Leonin Scimitar");
        harness.assertNotInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Cannot target a non-artifact card in a graveyard")
    void cannotTargetNonArtifactCard() {
        Card nonArtifactCard = new Shatter();
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
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
    @DisplayName("Cannot use a non-artifact permanent to pay the additional cost")
    void cannotSacrificeNonArtifact() {
        Card artifactCard = new Ornithopter();
        Permanent nonArtifact = harness.addToBattlefieldAndReturn(player1, new GoblinStriker());
        harness.setGraveyard(player1, List.of(artifactCard));
        harness.setHand(player1, List.of(new TrashForTreasure()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, artifactCard.getId(), nonArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact card in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        Card artifactCard = new Ornithopter();
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        harness.setGraveyard(player2, List.of(artifactCard));
        harness.setHand(player1, List.of(new TrashForTreasure()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, artifactCard.getId(), sacrificedArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Pays the sacrifice cost even if the graveyard target leaves before resolution")
    void paysSacrificeCostWhenTargetLeavesBeforeResolution() {
        Card artifactCard = new Ornithopter();
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        harness.setGraveyard(player1, List.of(artifactCard));
        harness.setHand(player1, List.of(new TrashForTreasure()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, artifactCard.getId(), sacrificedArtifact.getId());
        harness.assertInGraveyard(player1, "Alpha Myr");
        gd.playerGraveyards.get(player1.getId()).removeIf(card -> card.getId().equals(artifactCard.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Alpha Myr");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Alpha Myr");
        harness.assertInGraveyard(player1, "Trash for Treasure");
    }
}
