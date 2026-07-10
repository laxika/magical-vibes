package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DauntlessDourbark;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RootgrappleTest extends BaseCardTest {

    private void addFiveMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Destroys target noncreature permanent and does not draw without a Treefolk")
    void destroysNoncreatureNoTreefolkNoDraw() {
        harness.addToBattlefield(player2, new Spellbook());
        UUID targetId = harness.getPermanentId(player2, "Spellbook");
        harness.setHand(player1, List.of(new Rootgrapple()));
        addFiveMana();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        // No draw (hand = before - 1 spell cast)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1);
    }

    @Test
    @DisplayName("Destroys target noncreature permanent and draws a card when controlling a Treefolk")
    void destroysNoncreatureWithTreefolkDraws() {
        harness.addToBattlefield(player1, new DauntlessDourbark());
        harness.addToBattlefield(player2, new Spellbook());
        UUID targetId = harness.getPermanentId(player2, "Spellbook");
        harness.setHand(player1, List.of(new Rootgrapple()));
        addFiveMana();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        // Drew a card (hand = before - 1 spell cast + 1 draw)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Does not draw when only opponent controls a Treefolk")
    void opponentTreefolkDoesNotTriggerDraw() {
        harness.addToBattlefield(player2, new DauntlessDourbark());
        harness.addToBattlefield(player2, new Spellbook());
        UUID targetId = harness.getPermanentId(player2, "Spellbook");
        harness.setHand(player1, List.of(new Rootgrapple()));
        addFiveMana();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new Spellbook()); // legal target so spell is playable
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Rootgrapple()));
        addFiveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature permanent");
    }
}
