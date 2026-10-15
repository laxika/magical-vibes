package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.PrakhataClubSecurity;
import com.github.laxika.magicalvibes.cards.p.PrakhataPillarBug;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FortuitousFind.class, PropheticPrism.class, PrakhataClubSecurity.class, PrakhataPillarBug.class})
class FortuitousFindTest extends BaseCardTest {

    @Test
    void artifactModeReturnsArtifactCardToHand() {
        Card artifact = new PropheticPrism();
        Card creature = new PrakhataClubSecurity();
        harness.setGraveyard(player1, List.of(artifact, creature));
        harness.setHand(player1, List.of(new FortuitousFind()));
        addMana();

        harness.castSorcery(player1, 0, 0);

        assertThat(graveyardChoice().validCardIds()).containsExactly(artifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Prophetic Prism");
        harness.assertNotInGraveyard(player1, "Prophetic Prism");
        harness.assertInGraveyard(player1, "Prakhata Club Security");
    }

    @Test
    void creatureModeReturnsCreatureCardToHand() {
        Card artifact = new PropheticPrism();
        Card creature = new PrakhataClubSecurity();
        harness.setGraveyard(player1, List.of(artifact, creature));
        harness.setHand(player1, List.of(new FortuitousFind()));
        addMana();

        harness.castSorcery(player1, 0, 1);

        assertThat(graveyardChoice().validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Prakhata Club Security");
        harness.assertNotInGraveyard(player1, "Prakhata Club Security");
        harness.assertInGraveyard(player1, "Prophetic Prism");
    }

    @Test
    void bothModesReturnArtifactAndCreatureCardsToHand() {
        Card artifact = new PropheticPrism();
        Card creature = new PrakhataClubSecurity();
        harness.setGraveyard(player1, List.of(artifact, creature));
        harness.setHand(player1, List.of(new FortuitousFind()));
        addMana();

        harness.castSorcery(player1, 0, 2);

        assertThat(graveyardChoice().validCardIds()).containsExactly(artifact.getId(), creature.getId());
        assertThat(graveyardChoice().maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Prophetic Prism");
        harness.assertInHand(player1, "Prakhata Club Security");
        harness.assertNotInGraveyard(player1, "Prophetic Prism");
        harness.assertNotInGraveyard(player1, "Prakhata Club Security");
    }

    @Test
    void bothModeExcludesCardsThatAreNeitherArtifactsNorCreatures() {
        Card creature = new PrakhataClubSecurity();
        Card noncreature = new FortuitousFind();
        Card artifact = new PropheticPrism();
        harness.setGraveyard(player1, List.of(creature, noncreature, artifact));
        harness.setHand(player1, List.of(new FortuitousFind()));
        addMana();

        harness.castSorcery(player1, 0, 2);

        assertThat(graveyardChoice().validCardIds()).containsExactly(creature.getId(), artifact.getId());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void selectedModesCannotOmitRequiredTargets(int mode) {
        harness.setGraveyard(player1, List.of(new PropheticPrism(), new PrakhataClubSecurity()));
        harness.setHand(player1, List.of(new FortuitousFind()));
        addMana();

        harness.castSorcery(player1, 0, mode);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void cannotCastWithoutLegalTargets(int mode) {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new FortuitousFind()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, mode))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void bothModesRejectTwoCardsOfOnlyOneRequiredType(int type) {
        Card first = type == 0 ? new PropheticPrism() : new PrakhataClubSecurity();
        Card second = type == 0 ? new PropheticPrism() : new PrakhataClubSecurity();
        harness.setGraveyard(player1, List.of(first, second, new PrakhataPillarBug()));
        harness.setHand(player1, List.of(new FortuitousFind()));
        addMana();

        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesCanTargetTheSameArtifactCreature() {
        Card artifactCreature = new PrakhataPillarBug();
        harness.setGraveyard(player1, List.of(artifactCreature));
        harness.setHand(player1, List.of(new FortuitousFind()));
        addMana();

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(artifactCreature.getId(), artifactCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Prakhata Pillar-Bug");
        harness.assertNotInGraveyard(player1, "Prakhata Pillar-Bug");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifactCreature);
    }

    private PendingInteraction.MultiGraveyardChoice graveyardChoice() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        return gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
