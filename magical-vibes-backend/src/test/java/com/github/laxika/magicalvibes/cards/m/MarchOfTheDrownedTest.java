package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DireFleetCaptain;
import com.github.laxika.magicalvibes.cards.h.HeadstrongBrute;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MarchOfTheDrownedTest extends BaseCardTest {

    // ===== Mode 0: Return target creature card =====

    @Test
    @DisplayName("Mode 0 — prompts for creature target in graveyard")
    void mode0PromptsForCreatureTarget() {
        Card creature = new LlanowarElves();
        Card march = new MarchOfTheDrowned();
        harness.setGraveyard(player1, List.of(creature, march));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(1);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).contains(creature.getId());
        assertThat(gd.interaction.multiSelection().multiGraveyardMaxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mode 0 — selecting creature returns it to hand")
    void mode0ReturnsCreatureToHand() {
        Card creature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Mode 0 — no creatures in graveyard skips prompt")
    void mode0NoCreaturesSkipsPrompt() {
        harness.setGraveyard(player1, List.of(new MarchOfTheDrowned()));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Mode 1: Return two target Pirate cards =====

    @Test
    @DisplayName("Mode 1 — prompts for Pirate targets in graveyard")
    void mode1PromptsForPirateTargets() {
        Card pirate1 = new HeadstrongBrute();
        Card pirate2 = new DireFleetCaptain();
        Card nonPirate = new LlanowarElves();
        harness.setGraveyard(player1, List.of(pirate1, pirate2, nonPirate));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(2);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds())
                .contains(pirate1.getId(), pirate2.getId());
        assertThat(gd.interaction.multiSelection().multiGraveyardMaxCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mode 1 — selecting two Pirates returns them to hand")
    void mode1ReturnsTwoPiratesToHand() {
        Card pirate1 = new HeadstrongBrute();
        Card pirate2 = new DireFleetCaptain();
        harness.setGraveyard(player1, List.of(pirate1, pirate2));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1);
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Headstrong Brute");
        harness.assertInHand(player1, "Dire Fleet Captain");
        harness.assertNotInGraveyard(player1, "Headstrong Brute");
        harness.assertNotInGraveyard(player1, "Dire Fleet Captain");
    }

    @Test
    @DisplayName("Mode 1 — non-Pirate creatures are excluded from targets")
    void mode1ExcludesNonPirateCreatures() {
        Card pirate = new HeadstrongBrute();
        Card nonPirate = new LlanowarElves();
        harness.setGraveyard(player1, List.of(pirate, nonPirate));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(1);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).contains(pirate.getId());
    }

    @Test
    @DisplayName("Mode 1 — no Pirates in graveyard skips prompt")
    void mode1NoPiratesSkipsPrompt() {
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Spell goes to graveyard after resolution =====

    @Test
    @DisplayName("March of the Drowned goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        Card creature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "March of the Drowned");
    }
}
