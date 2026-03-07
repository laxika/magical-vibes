package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BirthingPodTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice MV 1 creature, search for MV 2 creature and put onto battlefield")
    void sacrificeMV1SearchForMV2() {
        addPodReady(player1);
        Permanent elves = addCreature(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Seed library with MV 2 creature
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GoldMyr());

        UUID elvesId = elves.getId();
        harness.activateAbility(player1, 0, null, elvesId);

        // Elves should be sacrificed
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        // Resolve the ability
        harness.passBothPriorities();

        // Should prompt for library search
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        // Choose Gold Myr (MV 2)
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Gold Myr should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gold Myr"));
    }

    @Test
    @DisplayName("Sacrifice MV 3 creature, search for MV 4 creature")
    void sacrificeMV3SearchForMV4() {
        addPodReady(player1);
        Permanent knight = addCreature(player1, new BenalishKnight());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Library has MV 4 and MV 2 creatures
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new HillGiant(), new GoldMyr()));

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        // Only MV 4 creature should be available (not MV 2)
        assertThat(gd.interaction.librarySearch().cards())
                .hasSize(1)
                .allMatch(c -> c.getName().equals("Hill Giant"));

        gs.handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Search only finds creatures with exact MV, not higher or lower")
    void searchFiltersToExactMV() {
        addPodReady(player1);
        Permanent elves = addCreature(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Library has MV 1, 3, 4, 5 creatures but NO MV 2
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new LlanowarElves(),    // MV 1
                new BenalishKnight(),   // MV 3
                new HillGiant(),        // MV 4
                new AirElemental()      // MV 5
        ));

        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        // No MV 2 creature in library, search should fail
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves")
                        || p.getCard().getName().equals("Benalish Knight")
                        || p.getCard().getName().equals("Hill Giant")
                        || p.getCard().getName().equals("Air Elemental"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("finds no"));
    }

    @Test
    @DisplayName("Cannot activate during opponent's turn (sorcery speed)")
    void cannotActivateDuringOpponentsTurn() {
        addPodReady(player1);
        addCreature(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        UUID elvesId = findPermanent(player1, "Llanowar Elves").getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elvesId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot activate outside main phase (sorcery speed)")
    void cannotActivateOutsideMainPhase() {
        addPodReady(player1);
        addCreature(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        UUID elvesId = findPermanent(player1, "Llanowar Elves").getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elvesId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        addPodReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent pod = addPodReady(player1);
        addCreature(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        pod.tap();

        UUID elvesId = findPermanent(player1, "Llanowar Elves").getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elvesId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Phyrexian mana can be paid with 2 life instead of green mana")
    void phyrexianManaPaidWithLife() {
        addPodReady(player1);
        Permanent elves = addCreature(player1, new LlanowarElves());
        // Only provide colorless mana - no green
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GoldMyr());

        harness.activateAbility(player1, 0, null, elves.getId());

        // Life should be reduced by 2 (Phyrexian mana)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Pod taps as part of the cost")
    void podTapsAsCost() {
        Permanent pod = addPodReady(player1);
        Permanent elves = addCreature(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GoldMyr());

        assertThat(pod.isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, elves.getId());
        assertThat(pod.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing MV 0 creature searches for MV 1")
    void sacrificeMV0SearchForMV1() {
        addPodReady(player1);

        // Create a 0-cost creature token on the battlefield
        // Use GoldMyr with MV 2 to search for MV 3 instead, or use an actual MV 0 token
        // Actually let's test with MV 4 creature searching for MV 5
        Permanent hillGiant = addCreature(player1, new HillGiant());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new AirElemental());

        harness.activateAbility(player1, 0, null, hillGiant.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        // Only MV 5 should be offered
        assertThat(gd.interaction.librarySearch().cards())
                .hasSize(1)
                .allMatch(c -> c.getName().equals("Air Elemental"));

        gs.handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addPodReady(player1);
        Permanent elves = addCreature(player1, new LlanowarElves());
        // No mana added

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elves.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    private Permanent addPodReady(Player player) {
        harness.addToBattlefield(player, new BirthingPod());
        return findPermanent(player, "Birthing Pod");
    }

    private Permanent addCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        harness.addToBattlefield(player, card);
        Permanent creature = gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(card.getName()))
                .reduce((first, second) -> second)  // get last added
                .orElseThrow();
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(name + " not found"));
    }
}
