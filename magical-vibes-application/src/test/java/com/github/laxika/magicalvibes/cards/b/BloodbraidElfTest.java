package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.ProsperTomeBound;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodbraidElf.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class, Mountain.class, Plains.class, ProsperTomeBound.class})
class BloodbraidElfTest extends BaseCardTest {

    @Test
    @DisplayName("Cascade skips lands and equal/greater-cost nonlands, stopping at the first lesser one")
    void cascadeDigsToFirstLesserNonland() {
        setupCasterTurn();

        // Bloodbraid Elf is {2}{R}{G} = mana value 4. Dig should skip the land and the MV-4 Hill Giant
        // (equal, not less), stop at Grizzly Bears (MV 2 < 4), and never touch the Elves beneath it.
        LlanowarElves belowHit = new LlanowarElves();
        harness.setLibrary(player1, List.of(
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
        harness.setLibrary(player1, List.of(land, skipped, new GrizzlyBears(), belowHit));

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

        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new Plains()));

        castBloodbraidElf();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Declining cascade bottoms the hit with the skipped cards beneath the untouched library")
    void decliningHitReturnsAllDugCardsToBottom() {
        setupCasterTurn();
        Mountain skipped = new Mountain();
        GrizzlyBears hit = new GrizzlyBears();
        LlanowarElves untouched = new LlanowarElves();
        harness.setLibrary(player1, List.of(skipped, hit, untouched));

        castBloodbraidElf();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skipped, hit);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bloodbraid Elf");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An empty library does not prevent Bloodbraid Elf from resolving")
    void emptyLibraryStillAllowsCreatureToResolve() {
        setupCasterTurn();
        harness.setLibrary(player1, List.of());

        castBloodbraidElf();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bloodbraid Elf");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bloodbraid Elf can attack on the turn it resolves")
    void hasteAllowsImmediateAttack() {
        setupCasterTurn();
        harness.setLibrary(player1, List.of());
        castBloodbraidElf();
        harness.passBothPriorities();
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(findPermanent(player1, "Bloodbraid Elf").isTapped()).isTrue();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Casting a cascade hit from exile triggers Prosper's Treasure ability")
    void cascadeHitTriggersPlayingFromExileAbility() {
        setupCasterTurn();
        harness.addToBattlefield(player1, new ProsperTomeBound());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        castBloodbraidElf();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Treasure");
    }

    private void setupCasterTurn() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
    }

    private void castBloodbraidElf() {
        harness.castFromHand(player1, new BloodbraidElf(), "{2}{R}{G}");
    }
}
