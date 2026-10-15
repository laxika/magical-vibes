package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JacesRuse.class, JaceArcaneStrategist.class, GrizzlyBears.class})
class JacesRuseTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two target creatures and finds Jace from the graveyard")
    void returnsTwoCreaturesAndFindsJaceFromGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        List<UUID> creatureIds = gd.playerBattlefields.get(player2.getId()).stream()
                .map(Permanent::getId)
                .toList();
        harness.setGraveyard(player1, List.of(new JaceArcaneStrategist()));

        cast(creatureIds);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Jace, Arcane Strategist");
        harness.assertNotInGraveyard(player1, "Jace, Arcane Strategist");
    }

    @Test
    @DisplayName("Can return only one target creature")
    void returnsOneCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Offers Jace from the library when the optional search is accepted")
    void searchesLibraryForJace() {
        harness.setLibrary(player1, List.of(new JaceArcaneStrategist()));

        cast(List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Jace, Arcane Strategist");
    }

    @Test
    @DisplayName("Rejects a non-creature target")
    void rejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new JacesRuse()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new JacesRuse()));
        addMana();
        harness.castSorcery(player1, 0, targetIds);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
