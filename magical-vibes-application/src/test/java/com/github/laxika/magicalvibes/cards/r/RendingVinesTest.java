package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RendingVinesTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an artifact whose mana value is within hand size and draws a card")
    void destroysEligibleArtifactAndDraws() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new RendingVines(), new Forest()));
        addMana();

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Does not destroy an artifact above hand size but still draws")
    void skipsIneligibleArtifactButDraws() {
        harness.addToBattlefield(player2, new IcyManipulator());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new RendingVines(), new Forest()));
        addMana();

        UUID targetId = harness.getPermanentId(player2, "Icy Manipulator");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Icy Manipulator");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Can destroy an enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new GhostlyPrison());
        harness.setHand(player1, List.of(new RendingVines(), new Forest(), new Forest(), new Forest()));
        addMana();

        UUID targetId = harness.getPermanentId(player2, "Ghostly Prison");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ghostly Prison");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RendingVines()));
        addMana();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzling from a missing target does not draw")
    void missingTargetFizzlingDoesNotDraw() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new RendingVines(), new Forest()));
        addMana();

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
