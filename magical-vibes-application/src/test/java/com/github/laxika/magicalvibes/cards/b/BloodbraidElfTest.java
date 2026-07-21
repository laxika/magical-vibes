package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodbraidElfTest extends BaseCardTest {

    @Test
    @DisplayName("Cascade skips lands and equal/greater-cost nonlands, stopping at the first lesser one")
    void cascadeDigsToFirstLesserNonland() {
        setupCasterTurn();

        // Bloodbraid Elf is {2}{R}{G} = mana value 4. Dig should skip the land and the MV-4 Hill Giant
        // (equal, not less), stop at Grizzly Bears (MV 2 < 4), and never touch the Elves beneath it.
        LlanowarElves belowHit = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new Mountain(), new HillGiant(), new GrizzlyBears(), belowHit));

        castBloodbraidElf();
        harness.passBothPriorities(); // resolve the cascade trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Grizzly Bears");

        // The dig stopped at the hit — the card beneath it stays on the library.
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowHit);
    }

    @Test
    @DisplayName("Casting the cascade hit puts it on the stack for free; the rest go to the bottom")
    void castingHitPutsItOnStack() {
        setupCasterTurn();

        LlanowarElves belowHit = new LlanowarElves();
        Mountain land = new Mountain();
        HillGiant skipped = new HillGiant();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(land, skipped, new GrizzlyBears(), belowHit));

        castBloodbraidElf();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Grizzly Bears")
                && se.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(belowHit, land, skipped);
    }

    @Test
    @DisplayName("Cascade with no qualifying nonland bottoms everything and prompts nothing")
    void noQualifyingCardBottomsEverything() {
        setupCasterTurn();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new Mountain(), new Plains()));

        castBloodbraidElf();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    private void setupCasterTurn() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
    }

    private void castBloodbraidElf() {
        harness.setHand(player1, List.of(new BloodbraidElf()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
