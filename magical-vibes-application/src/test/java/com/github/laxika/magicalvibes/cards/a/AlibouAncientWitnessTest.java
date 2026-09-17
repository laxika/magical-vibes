package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AlibouAncientWitness.class)
class AlibouAncientWitnessTest extends BaseCardTest {

    @Test
    @DisplayName("Gives other artifact creatures haste, but not itself or nonartifact creatures")
    void grantsHasteToOtherArtifactCreatures() {
        Permanent alibou = harness.addToBattlefieldAndReturn(player1, new AlibouAncientWitness());
        Permanent artifactCreature = addCreatureReady(player1, artifactCreature("Artifact Creature"));
        Permanent nonartifactCreature = addCreatureReady(player1, creature("Nonartifact Creature"));

        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, alibou, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonartifactCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Deals damage and scries for the number of tapped artifacts when an artifact creature attacks")
    void artifactCreatureAttackDealsDamageAndScriesForTappedArtifacts() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AlibouAncientWitness());
        addCreatureReady(player1, artifactCreature("Artifact Creature 1"));
        addCreatureReady(player1, artifactCreature("Artifact Creature 2"));

        Permanent tappedArtifact = harness.addToBattlefieldAndReturn(player1, artifact("Tapped Artifact"));
        tappedArtifact.tap();
        harness.addToBattlefield(player1, artifact("Untapped Artifact"));
        harness.setLibrary(player1, List.of(new Card(), new Card(), new Card(), new Card()));

        declareAttackers(List.of(1, 2));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(3);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when only nonartifact creatures attack")
    void doesNotTriggerForNonartifactAttackers() {
        harness.addToBattlefield(player1, new AlibouAncientWitness());
        addCreatureReady(player1, creature("Nonartifact Creature"));

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Card artifactCreature(String name) {
        Card card = artifact(name);
        card.setAdditionalTypes(Set.of(CardType.CREATURE));
        card.setPower(0);
        card.setToughness(2);
        return card;
    }

    private Card artifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        return card;
    }

    private Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
