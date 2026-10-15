package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimDiscovery.class, GiantScorpion.class, Forest.class})
class GrimDiscoveryTest extends BaseCardTest {

    @Test
    void creatureModeReturnsCreatureCardToHand() {
        Card creature = new GiantScorpion();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setHand(player1, List.of(new GrimDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Giant Scorpion");
        harness.assertNotInGraveyard(player1, "Giant Scorpion");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void landModeReturnsLandCardToHand() {
        Card creature = new GiantScorpion();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setHand(player1, List.of(new GrimDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(land.getId());

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Giant Scorpion");
    }

    @Test
    void bothModeReturnsCreatureAndLandCardsToHand() {
        Card creature = new GiantScorpion();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setHand(player1, List.of(new GrimDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(creature.getId(), land.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount())
                .isEqualTo(2);

        List<UUID> selectedIds = new ArrayList<>(List.of(creature.getId(), land.getId()));
        harness.handleMultipleCardsChosen(player1, selectedIds);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Giant Scorpion");
        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Giant Scorpion");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void bothModeExcludesCardsThatAreNeitherCreaturesNorLands() {
        Card creature = new GiantScorpion();
        Card land = new Forest();
        Card sorcery = new GrimDiscovery();
        harness.setGraveyard(player1, List.of(creature, land, sorcery));
        harness.setHand(player1, List.of(new GrimDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(creature.getId(), land.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sorcery);
        assertThat(gd.playerHands.get(player1.getId())).contains(creature, land).doesNotContain(sorcery);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void selectedModesRequireTargets(int mode) {
        Card creature = new GiantScorpion();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setHand(player1, List.of(new GrimDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, mode);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModeRequiresBothTargets() {
        Card creature = new GiantScorpion();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setHand(player1, List.of(new GrimDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModeCannotTargetTwoCreatures() {
        Card firstCreature = new GiantScorpion();
        Card secondCreature = new GiantScorpion();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature, land));
        harness.setHand(player1, List.of(new GrimDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModeCannotTargetTwoLands() {
        Card creature = new GiantScorpion();
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setGraveyard(player1, List.of(creature, firstLand, secondLand));
        harness.setHand(player1, List.of(new GrimDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstLand.getId(), secondLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCardsInOpponentsGraveyard() {
        Card creature = new GiantScorpion();
        Card opposingCreature = new GiantScorpion();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setHand(player1, List.of(new GrimDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opposingCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
    }

    @Test
    void bothModeReturnsRemainingLegalTarget() {
        Card creature = new GiantScorpion();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setHand(player1, List.of(new GrimDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));
        harness.setGraveyard(player1, List.of(land));
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land).doesNotContain(creature);
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(creature);
    }

}
