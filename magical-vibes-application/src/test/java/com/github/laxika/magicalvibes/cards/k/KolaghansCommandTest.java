package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KolaghansCommand.class, FountainOfYouth.class, Forest.class, GrizzlyBears.class})
class KolaghansCommandTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature and makes a target player discard")
    void returnsCreatureAndMakesPlayerDiscard() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));
        castWithModes(new int[]{0, 1}, creature.getId(), List.of(player2.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Destroys an artifact and deals 2 damage to a player")
    void destroysArtifactAndDealsDamage() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        castWithModes(new int[]{2, 3}, null, List.of(
                harness.getPermanentId(player2, "Fountain of Youth"), player2.getId()));

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Allows two modes to target the same player")
    void allowsSharedPlayerTarget() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));
        castWithModes(new int[]{1, 3}, null, List.of(player2.getId(), player2.getId()));

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Destroy artifact mode rejects a nonartifact target")
    void destroyArtifactModeRejectsNonartifactTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KolaghansCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 2, new int[]{2, 3}, null,
                List.of(creature.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    private void castWithModes(int[] modes, java.util.UUID targetId, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new KolaghansCommand()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 2, modes, targetId, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
