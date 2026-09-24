package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeaconOfUnrest.class, GrizzlyBears.class, AngelsFeather.class, HolyDay.class})
class BeaconOfUnrestTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Beacon of Unrest with a graveyard target puts it on the stack")
    void castingPutsItOnStack() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Returns creature from own graveyard to battlefield")
    void returnsCreatureFromOwnGraveyard() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns artifact from own graveyard to battlefield")
    void returnsArtifactFromOwnGraveyard() {
        AngelsFeather artifact = new AngelsFeather();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, artifact.getId());

        harness.assertOnBattlefield(player1, "Angel's Feather");
        harness.assertNotInGraveyard(player1, "Angel's Feather");
    }

    @Test
    @DisplayName("Returns creature from opponent's graveyard under your control")
    void returnsCreatureFromOpponentGraveyard() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Beacon is shuffled into library instead of going to graveyard")
    void shuffledIntoLibraryNotGraveyard() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 0, creature.getId());

        harness.assertNotInGraveyard(player1, "Beacon of Unrest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Beacon of Unrest"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("shuffled into its owner's library"));
    }

    @Test
    @DisplayName("Resolution state is fully consumed after resolution")
    void noDanglingResumptionAfterResolution() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, creature.getId());

        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Beacon of Unrest"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Cannot cast when graveyards have no artifact or creature cards")
    void noValidTargetsInGraveyards() {
        HolyDay invalidTarget = new HolyDay();
        harness.setGraveyard(player1, List.of(invalidTarget));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, invalidTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Beacon of Unrest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target cards across both graveyards")
    void targetsAcrossBothGraveyards() {
        GrizzlyBears creature = new GrizzlyBears();
        AngelsFeather artifact = new AngelsFeather();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(artifact));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, artifact.getId());

        harness.assertOnBattlefield(player1, "Angel's Feather");
        harness.assertNotInGraveyard(player2, "Angel's Feather");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, creature.getId());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not choose a different card if the target leaves the graveyard before resolution")
    void doesNotRetargetWhenTargetLeavesGraveyard() {
        GrizzlyBears target = new GrizzlyBears();
        AngelsFeather otherMatchingCard = new AngelsFeather();
        harness.setGraveyard(player1, List.of(target));
        harness.setGraveyard(player2, List.of(otherMatchingCard));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castSorcery(player1, 0, 0, target.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Angel's Feather");
        harness.assertInGraveyard(player2, "Angel's Feather");
        harness.assertInGraveyard(player1, "Beacon of Unrest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }
}
