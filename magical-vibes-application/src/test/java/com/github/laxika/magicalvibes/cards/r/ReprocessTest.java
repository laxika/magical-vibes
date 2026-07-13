package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Opposition;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReprocessTest extends BaseCardTest {

    @Test
    @DisplayName("Only artifacts, creatures, and lands the controller controls are sacrificeable")
    void promptsSacrificeChoiceForEligibleTypes() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefieldAndReturn(player1, new Opposition()); // enchantment — not eligible
        setupLibrary();
        castReprocess();

        harness.passBothPriorities(); // resolve Reprocess

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                artifact.getId(), creature.getId(), land.getId());
    }

    @Test
    @DisplayName("Draws a card for each permanent sacrificed")
    void drawsPerPermanentSacrificed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        setupLibrary();
        castReprocess();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId(), land.getId()));

        // The two chosen permanents are gone; the artifact remains
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        // Two permanents sacrificed → two cards drawn
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing nothing draws nothing and keeps all permanents")
    void sacrificeNoneDrawsNothing() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new Forest());
        setupLibrary();
        castReprocess();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no eligible permanents, the spell resolves with no prompt")
    void noEligiblePermanentsNoPrompt() {
        harness.addToBattlefieldAndReturn(player1, new Opposition()); // enchantment only
        setupLibrary();
        castReprocess();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Helpers =====

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Island(), new Island(), new Island(), new Island()));
    }

    private void castReprocess() {
        harness.setHand(player1, List.of(new Reprocess()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, 0);
    }
}
