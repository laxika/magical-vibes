package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScrapTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact")
    void destroysTargetArtifact() {
        Permanent artifact = addArtifact();
        harness.setHand(player1, List.of(new Scrap()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Test Artifact");
        harness.assertInGraveyard(player2, "Test Artifact");
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifactPermanent() {
        Card creatureCard = new Card();
        creatureCard.setName("Test Creature");
        creatureCard.setType(CardType.CREATURE);
        Permanent creature = new Permanent(creatureCard);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new Scrap()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Scrap and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Scrap()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scrap");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addArtifact() {
        Card artifactCard = new Card();
        artifactCard.setName("Test Artifact");
        artifactCard.setType(CardType.ARTIFACT);
        Permanent artifact = new Permanent(artifactCard);
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        return artifact;
    }
}
