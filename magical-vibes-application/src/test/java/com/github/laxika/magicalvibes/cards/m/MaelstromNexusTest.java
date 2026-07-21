package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaelstromNexusTest extends BaseCardTest {

    @Test
    @DisplayName("The first spell each turn cascades off that spell's mana value, not the Nexus's")
    void firstSpellGetsCascadeKeyedToTheSpell() {
        setupCasterTurn();
        harness.addToBattlefield(player1, new MaelstromNexus());

        // Grizzly Bears is {1}{G} = mana value 2. Cascade must dig with a threshold of 2 (the spell's
        // mana value), so the MV-4 Hill Giant is skipped and Llanowar Elves (MV 1 < 2) is the hit. If the
        // grant wrongly used the Nexus's own mana value (5), the Hill Giant (4 < 5) would qualify instead.
        LlanowarElves belowHit = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new HillGiant(), belowHit));

        castGrizzlyBears();
        harness.passBothPriorities(); // resolve the cascade trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Llanowar Elves");
    }

    @Test
    @DisplayName("Only the first spell each turn cascades — the second gets nothing")
    void secondSpellDoesNotCascade() {
        setupCasterTurn();
        harness.addToBattlefield(player1, new MaelstromNexus());

        // First spell: an all-land library means the cascade fires but finds no hit (no prompt).
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        castGrizzlyBears();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        // Second spell of the turn, with a hittable nonland on top of the library. No cascade must fire.
        setupCasterTurn();
        LlanowarElves untouched = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(untouched);
        castGrizzlyBears();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
    }

    @Test
    @DisplayName("Without the Nexus, the first spell has no cascade")
    void noCascadeWithoutTheNexus() {
        setupCasterTurn();

        LlanowarElves untouched = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(untouched);

        castGrizzlyBears();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
    }

    private void setupCasterTurn() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }
}
