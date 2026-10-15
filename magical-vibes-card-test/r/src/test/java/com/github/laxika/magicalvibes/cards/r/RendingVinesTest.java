package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RendingVines.class, FountainOfYouth.class, Forest.class, GhostlyPrison.class,
        GrizzlyBears.class, IcyManipulator.class})
class RendingVinesTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an artifact whose mana value is within hand size and draws a card")
    void destroysEligibleArtifactAndDraws() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new RendingVines(), new Forest()));
        addMana();

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castAndResolveInstant(player1, 0, targetId);

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
        harness.castAndResolveInstant(player1, 0, targetId);

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
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Ghostly Prison");
    }

    @Test
    @DisplayName("Rechecks hand size on resolution before destroying an enchantment")
    void rechecksHandSizeOnResolution() {
        harness.addToBattlefield(player2, new GhostlyPrison());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new RendingVines(), new Forest(), new Forest()));
        addMana();

        UUID targetId = harness.getPermanentId(player2, "Ghostly Prison");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Ghostly Prison");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
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

    @Test
    @DisplayName("Destroys a zero-mana artifact with an empty hand, including your own artifact")
    void destroysOwnZeroManaArtifactWithEmptyHand() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new RendingVines()));
        harness.setHand(player2, List.of());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        addMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Fountain of Youth"));

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Rending Vines");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("An increase in hand size while on the stack enables destruction at equality")
    void increasedHandSizeEnablesDestruction() {
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.setHand(player1, List.of(new RendingVines()));
        harness.setHand(player2, List.of());
        addMana();
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Icy Manipulator"));

        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Icy Manipulator");
        harness.assertInGraveyard(player2, "Icy Manipulator");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A decrease in hand size while on the stack prevents destruction but not the draw")
    void decreasedHandSizePreventsDestruction() {
        harness.addToBattlefield(player2, new GhostlyPrison());
        harness.setHand(player1, List.of(new RendingVines(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        addMana();
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Ghostly Prison"));

        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ghostly Prison");
        harness.assertNotInGraveyard(player2, "Ghostly Prison");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
