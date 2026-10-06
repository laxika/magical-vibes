package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DauntlessDourbark;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WanderersTwig;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rootgrapple.class, DauntlessDourbark.class, WanderersTwig.class, Forest.class})
class RootgrappleTest extends BaseCardTest {

    private void addFiveMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Destroys target noncreature permanent and does not draw without a Treefolk")
    void destroysNoncreatureNoTreefolkNoDraw() {
        harness.addToBattlefield(player2, new WanderersTwig());
        UUID targetId = harness.getPermanentId(player2, "Wanderer's Twig");
        harness.setHand(player1, List.of(new Rootgrapple()));
        addFiveMana();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Wanderer's Twig");
        // No draw (hand = before - 1 spell cast)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1);
    }

    @Test
    @DisplayName("Destroys target noncreature permanent and draws a card when controlling a Treefolk")
    void destroysNoncreatureWithTreefolkDraws() {
        harness.addToBattlefield(player1, new DauntlessDourbark());
        harness.addToBattlefield(player2, new WanderersTwig());
        UUID targetId = harness.getPermanentId(player2, "Wanderer's Twig");
        harness.setHand(player1, List.of(new Rootgrapple()));
        addFiveMana();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Wanderer's Twig");
        // Drew a card (hand = before - 1 spell cast + 1 draw)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Does not draw when only opponent controls a Treefolk")
    void opponentTreefolkDoesNotTriggerDraw() {
        harness.addToBattlefield(player2, new DauntlessDourbark());
        harness.addToBattlefield(player2, new WanderersTwig());
        UUID targetId = harness.getPermanentId(player2, "Wanderer's Twig");
        harness.setHand(player1, List.of(new Rootgrapple()));
        addFiveMana();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Wanderer's Twig");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new WanderersTwig()); // legal target so spell is playable
        harness.addToBattlefield(player2, new DauntlessDourbark());
        UUID targetId = harness.getPermanentId(player2, "Dauntless Dourbark");
        harness.setHand(player1, List.of(new Rootgrapple()));
        addFiveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature permanent");
    }

    @Test
    @DisplayName("Destroys a land controlled by the caster")
    void destroysOwnLand() {
        harness.addToBattlefield(player1, new Forest());
        UUID targetId = harness.getPermanentId(player1, "Forest");
        harness.setHand(player1, List.of(new Rootgrapple()));
        addFiveMana();

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws only one card even with multiple Treefolk")
    void multipleTreefolkDrawOnlyOneCard() {
        harness.addToBattlefield(player1, new DauntlessDourbark());
        harness.addToBattlefield(player1, new DauntlessDourbark());
        harness.addToBattlefield(player2, new WanderersTwig());
        UUID targetId = harness.getPermanentId(player2, "Wanderer's Twig");
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Rootgrapple()));
        addFiveMana();

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Wanderer's Twig");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw if its only target leaves the battlefield")
    void missingTargetPreventsDraw() {
        harness.addToBattlefield(player1, new DauntlessDourbark());
        harness.addToBattlefield(player2, new WanderersTwig());
        UUID targetId = harness.getPermanentId(player2, "Wanderer's Twig");
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Rootgrapple()));
        addFiveMana();

        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Rootgrapple");
    }

    @Test
    @DisplayName("Checks Treefolk control at resolution rather than casting")
    void treefolkEnteringBeforeResolutionAllowsDraw() {
        harness.addToBattlefield(player2, new WanderersTwig());
        UUID targetId = harness.getPermanentId(player2, "Wanderer's Twig");
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Rootgrapple()));
        addFiveMana();

        harness.castInstant(player1, 0, targetId);
        harness.addToBattlefield(player1, new DauntlessDourbark());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Wanderer's Twig");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw if the last controlled Treefolk leaves before resolution")
    void treefolkLeavingBeforeResolutionPreventsDraw() {
        harness.addToBattlefield(player1, new DauntlessDourbark());
        harness.addToBattlefield(player2, new WanderersTwig());
        UUID targetId = harness.getPermanentId(player2, "Wanderer's Twig");
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Rootgrapple()));
        addFiveMana();

        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Wanderer's Twig");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
