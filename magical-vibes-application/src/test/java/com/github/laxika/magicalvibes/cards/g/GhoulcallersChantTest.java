package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhoulcallersChant.class, DiregrafGhoul.class, WalkingCorpse.class, AvacynsPilgrim.class})
class GhoulcallersChantTest extends BaseCardTest {


    @Test
    @DisplayName("Mode 0 — prompts for creature target in graveyard")
    void mode0PromptsForCreatureTarget() {
        Card creature = new AvacynsPilgrim();
        Card chant = new GhoulcallersChant();
        harness.setGraveyard(player1, List.of(creature, chant));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0); // mode 0 = creature

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        // Only creature should be valid, not the sorcery
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).contains(creature.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mode 0 — selecting creature returns it to hand")
    void mode0ReturnsCreatureToHand() {
        Card creature = new AvacynsPilgrim();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Avacyn's Pilgrim");
        harness.assertNotInGraveyard(player1, "Avacyn's Pilgrim");
    }

    @Test
    @DisplayName("Mode 0 requires a creature in your graveyard")
    void mode0RequiresCreature() {
        harness.setGraveyard(player1, List.of(new GhoulcallersChant()));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Mode 1 — prompts for Zombie targets in graveyard")
    void mode1PromptsForZombieTargets() {
        Card zombie1 = new DiregrafGhoul();
        Card zombie2 = new WalkingCorpse();
        Card nonZombie = new AvacynsPilgrim();
        harness.setGraveyard(player1, List.of(zombie1, zombie2, nonZombie));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1); // mode 1 = two zombies

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        // Only zombies should be valid targets
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .contains(zombie1.getId(), zombie2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mode 1 — selecting two Zombies returns them to hand")
    void mode1ReturnsTwoZombiesToHand() {
        Card zombie1 = new DiregrafGhoul();
        Card zombie2 = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(zombie1, zombie2));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1);
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Diregraf Ghoul");
        harness.assertInHand(player1, "Walking Corpse");
        harness.assertNotInGraveyard(player1, "Diregraf Ghoul");
        harness.assertNotInGraveyard(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("Mode 1 — non-Zombie creatures are excluded from targets")
    void mode1ExcludesNonZombieCreatures() {
        Card zombie = new DiregrafGhoul();
        Card secondZombie = new WalkingCorpse();
        Card nonZombie = new AvacynsPilgrim();
        harness.setGraveyard(player1, List.of(zombie, secondZombie, nonZombie));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactlyInAnyOrder(zombie.getId(), secondZombie.getId());
    }

    @Test
    @DisplayName("Mode 1 requires Zombies in your graveyard")
    void mode1RequiresZombies() {
        harness.setGraveyard(player1, List.of(new AvacynsPilgrim()));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    void mode0CannotChooseZeroTargets() {
        harness.setGraveyard(player1, List.of(new AvacynsPilgrim()));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mode1RequiresTwoAvailableZombies() {
        harness.setGraveyard(player1, List.of(new DiregrafGhoul()));
        harness.setGraveyard(player2, List.of(new WalkingCorpse()));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mode1CannotChooseOnlyOneZombie() {
        Card zombie = new DiregrafGhoul();
        harness.setGraveyard(player1, List.of(zombie, new WalkingCorpse()));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(zombie.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mode1CannotChooseZeroZombies() {
        harness.setGraveyard(player1, List.of(new DiregrafGhoul(), new WalkingCorpse()));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mode0ExcludesOpponentsGraveyard() {
        Card ownCreature = new AvacynsPilgrim();
        Card opposingCreature = new DiregrafGhoul();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(ownCreature.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opposingCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mode1ReturnsRemainingLegalTarget() {
        Card zombie1 = new DiregrafGhoul();
        Card zombie2 = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(zombie1, zombie2));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(zombie1.getId(), zombie2.getId()));
        harness.setGraveyard(player1, List.of(zombie2));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Walking Corpse");
        harness.assertNotInHand(player1, "Diregraf Ghoul");
        harness.assertInGraveyard(player1, "Ghoulcaller's Chant");
    }

    @Test
    @DisplayName("Ghoulcaller's Chant goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        Card creature = new AvacynsPilgrim();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GhoulcallersChant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghoulcaller's Chant");
    }
}
