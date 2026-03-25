package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
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

class GhoulcallersChantTest extends BaseCardTest {

    // ===== Mode 0: Return target creature card =====

    @Test
    @DisplayName("Mode 0 — prompts for creature target in graveyard")
    void mode0PromptsForCreatureTarget() {
        Card creature = new LlanowarElves();
        Card chant = new GhoulcallersChant();
        harness.setGraveyard(player1, List.of(creature, chant));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0); // mode 0 = creature

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        // Only creature should be valid, not the sorcery
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(1);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).contains(creature.getId());
        assertThat(gd.interaction.multiSelection().multiGraveyardMaxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mode 0 — selecting creature returns it to hand")
    void mode0ReturnsCreatureToHand() {
        Card creature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
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
        harness.setGraveyard(player1, List.of(new GhoulcallersChant()));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Mode 1: Return two target Zombie cards =====

    @Test
    @DisplayName("Mode 1 — prompts for Zombie targets in graveyard")
    void mode1PromptsForZombieTargets() {
        Card zombie1 = new DiregrafGhoul();
        Card zombie2 = new ScatheZombies();
        Card nonZombie = new LlanowarElves();
        harness.setGraveyard(player1, List.of(zombie1, zombie2, nonZombie));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1); // mode 1 = two zombies

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        // Only zombies should be valid targets
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(2);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds())
                .contains(zombie1.getId(), zombie2.getId());
        assertThat(gd.interaction.multiSelection().multiGraveyardMaxCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mode 1 — selecting two Zombies returns them to hand")
    void mode1ReturnsTwoZombiesToHand() {
        Card zombie1 = new DiregrafGhoul();
        Card zombie2 = new ScatheZombies();
        harness.setGraveyard(player1, List.of(zombie1, zombie2));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1);
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Diregraf Ghoul");
        harness.assertInHand(player1, "Scathe Zombies");
        harness.assertNotInGraveyard(player1, "Diregraf Ghoul");
        harness.assertNotInGraveyard(player1, "Scathe Zombies");
    }

    @Test
    @DisplayName("Mode 1 — non-Zombie creatures are excluded from targets")
    void mode1ExcludesNonZombieCreatures() {
        Card zombie = new DiregrafGhoul();
        Card nonZombie = new LlanowarElves();
        harness.setGraveyard(player1, List.of(zombie, nonZombie));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(1);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).contains(zombie.getId());
    }

    @Test
    @DisplayName("Mode 1 — no Zombies in graveyard skips prompt")
    void mode1NoZombiesSkipsPrompt() {
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Spell goes to graveyard after resolution =====

    @Test
    @DisplayName("Ghoulcaller's Chant goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        Card creature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghoulcaller's Chant");
    }
}
