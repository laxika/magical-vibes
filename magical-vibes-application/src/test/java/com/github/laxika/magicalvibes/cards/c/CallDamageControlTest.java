package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallDamageControl.class, Forest.class, GrizzlyBears.class, LeoninScimitar.class,
        Ornithopter.class, Pacifism.class})
class CallDamageControlTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two target artifact, creature, enchantment, and/or land cards")
    void returnsUpToTwoCardsOfChosenTypes() {
        Card artifact = new LeoninScimitar();
        Card creature = new GrizzlyBears();
        Card enchantment = new Pacifism();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(artifact, creature, enchantment, land));
        harness.setHand(player1, List.of(new CallDamageControl()));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                artifact.getId(), creature.getId(), enchantment.getId(), land.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId(), land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leonin Scimitar");
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertInGraveyard(player1, "Call Damage Control");
    }

    @Test
    @DisplayName("Allows choosing only one target")
    void allowsOneTarget() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CallDamageControl()));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Call Damage Control");
    }

    @Test
    @DisplayName("Rejects two targets of the same listed card type")
    void rejectsTwoTargetsOfTheSameType() {
        Card firstArtifact = new LeoninScimitar();
        Card secondArtifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(firstArtifact, secondArtifact));
        harness.setHand(player1, List.of(new CallDamageControl()));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstArtifact.getId(), secondArtifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more than one artifact");
    }

    @Test
    @DisplayName("Resolves with no targets when the graveyard has no eligible cards")
    void resolvesWithNoEligibleCards() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new CallDamageControl()));
        addManaForSpell();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.assertInGraveyard(player1, "Call Damage Control");
    }

    @Test
    void returnsCreatureAndEnchantment() {
        Card creature = new GrizzlyBears();
        Card enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(creature, enchantment));
        harness.setHand(player1, List.of(new CallDamageControl()));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), enchantment.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Pacifism");
    }

    @Test
    void allowsArtifactCreatureAndAnotherCreatureUnderDifferentModes() {
        Card artifactCreature = new Ornithopter();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifactCreature, creature));
        harness.setHand(player1, List.of(new CallDamageControl()));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(artifactCreature.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void allowsSameArtifactCreatureAsTargetOfBothModes() {
        Card artifactCreature = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifactCreature));
        harness.setHand(player1, List.of(new CallDamageControl()));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(artifactCreature.getId(), artifactCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifactCreature);
        harness.assertInGraveyard(player1, "Call Damage Control");
    }

    @Test
    void allowsZeroTargetsEvenWhenEligibleCardsExist() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CallDamageControl()));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Call Damage Control");
    }

    @Test
    void excludesOpponentCardsAndSorceriesFromTargetChoices() {
        Card creature = new GrizzlyBears();
        Card sorcery = new CallDamageControl();
        Card opposingArtifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, sorcery));
        harness.setGraveyard(player2, List.of(opposingArtifact));
        harness.setHand(player1, List.of(new CallDamageControl()));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sorcery);
    }

    @Test
    void returnsRemainingTargetWhenAnotherTargetLeavesGraveyard() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setHand(player1, List.of(new CallDamageControl()));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));
        harness.setGraveyard(player1, List.of(land));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Call Damage Control");
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
