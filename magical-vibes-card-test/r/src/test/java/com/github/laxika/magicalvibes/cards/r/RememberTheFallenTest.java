package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.l.LoxodonConvert;
import com.github.laxika.magicalvibes.cards.s.ShrineOfLoyalLegions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RememberTheFallen.class, LoxodonConvert.class, ShrineOfLoyalLegions.class, PorcelainLegionnaire.class})
class RememberTheFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 — creature in graveyard prompts for creature target")
    void mode0PromptsForCreatureTarget() {
        Card creature = new LoxodonConvert();
        Card artifact = new ShrineOfLoyalLegions();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0); // mode 0 = creature

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        // Only creature should be a valid target
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).contains(creature.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mode 0 — selecting creature returns it to hand")
    void mode0ReturnsCreatureToHand() {
        Card creature = new LoxodonConvert();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Loxodon Convert");
        harness.assertNotInGraveyard(player1, "Loxodon Convert");
    }

    @Test
    @DisplayName("Mode 0 cannot be cast without a legal target")
    void mode0CannotCastWithoutLegalTarget() {
        harness.setGraveyard(player1, List.of(new ShrineOfLoyalLegions()));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 1 — artifact in graveyard prompts for artifact target")
    void mode1PromptsForArtifactTarget() {
        Card creature = new LoxodonConvert();
        Card artifact = new ShrineOfLoyalLegions();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 1); // mode 1 = artifact

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        // Only artifact should be a valid target
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).contains(artifact.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mode 1 — selecting artifact returns it to hand")
    void mode1ReturnsArtifactToHand() {
        Card artifact = new ShrineOfLoyalLegions();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 1);
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shrine of Loyal Legions");
        harness.assertNotInGraveyard(player1, "Shrine of Loyal Legions");
    }

    @Test
    @DisplayName("Mode 1 cannot be cast without a legal target")
    void mode1CannotCastWithoutLegalTarget() {
        harness.setGraveyard(player1, List.of(new LoxodonConvert()));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 2 — both creature and artifact in graveyard prompts with both valid")
    void mode2PromptsForBothTargets() {
        Card creature = new LoxodonConvert();
        Card artifact = new ShrineOfLoyalLegions();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 2); // mode 2 = both

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .contains(creature.getId(), artifact.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mode 2 — selecting both creature and artifact returns them to hand")
    void mode2ReturnsBothToHand() {
        Card creature = new LoxodonConvert();
        Card artifact = new ShrineOfLoyalLegions();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 2);
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Loxodon Convert");
        harness.assertInHand(player1, "Shrine of Loyal Legions");
        harness.assertNotInGraveyard(player1, "Loxodon Convert");
        harness.assertNotInGraveyard(player1, "Shrine of Loyal Legions");
    }

    @Test
    @DisplayName("Mode 2 — non-creature/non-artifact cards are excluded from targets")
    void mode2ExcludesNonCreatureNonArtifactCards() {
        Card creature = new LoxodonConvert();
        Card artifact = new ShrineOfLoyalLegions();
        Card sorcery = new RememberTheFallen();
        harness.setGraveyard(player1, List.of(creature, artifact, sorcery));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        // The sorcery is neither a creature nor an artifact.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).containsExactlyInAnyOrder(creature.getId(), artifact.getId());
    }

    @Test
    @DisplayName("Remember the Fallen goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        Card creature = new LoxodonConvert();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Remember the Fallen");
    }

    @Test
    void creatureModeRequiresOneTarget() {
        harness.setGraveyard(player1, List.of(new LoxodonConvert()));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void artifactModeRequiresOneTarget() {
        harness.setGraveyard(player1, List.of(new ShrineOfLoyalLegions()));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 1);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesRequireBothTargets() {
        Card creature = new LoxodonConvert();
        Card artifact = new ShrineOfLoyalLegions();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesCannotReturnTwoNonartifactCreatures() {
        Card first = new LoxodonConvert();
        Card second = new LoxodonConvert();
        Card artifact = new ShrineOfLoyalLegions();
        harness.setGraveyard(player1, List.of(first, second, artifact));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesCannotReturnTwoNoncreatureArtifacts() {
        Card first = new ShrineOfLoyalLegions();
        Card second = new ShrineOfLoyalLegions();
        Card creature = new LoxodonConvert();
        harness.setGraveyard(player1, List.of(first, second, creature));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesCanTargetTheSameArtifactCreature() {
        Card artifactCreature = new PorcelainLegionnaire();
        harness.setGraveyard(player1, List.of(artifactCreature));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(artifactCreature.getId(), artifactCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifactCreature);
        harness.assertNotInGraveyard(player1, "Porcelain Legionnaire");
        harness.assertInGraveyard(player1, "Remember the Fallen");
    }

    @Test
    void bothModesReturnTheRemainingLegalTarget() {
        Card creature = new LoxodonConvert();
        Card artifact = new ShrineOfLoyalLegions();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), artifact.getId()));
        harness.setGraveyard(player1, List.of(artifact));
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        harness.assertInGraveyard(player1, "Remember the Fallen");
    }

    @Test
    void bothModesExcludeOpponentsGraveyard() {
        Card creature = new LoxodonConvert();
        Card artifact = new ShrineOfLoyalLegions();
        Card opposingCreature = new LoxodonConvert();
        Card opposingArtifact = new ShrineOfLoyalLegions();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setGraveyard(player2, List.of(opposingCreature, opposingArtifact));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactlyInAnyOrder(creature.getId(), artifact.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(opposingCreature.getId(), opposingArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
