package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BloodbraidElf;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaelstromNexus.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class, Hurricane.class, BloodbraidElf.class, Shock.class})
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
        harness.setLibrary(player1, List.of(new HillGiant(), belowHit));

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
        harness.setLibrary(player1, List.of(new Forest()));
        castGrizzlyBears();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        // Second spell of the turn, with a hittable nonland on top of the library. No cascade must fire.
        setupCasterTurn();
        LlanowarElves untouched = new LlanowarElves();
        harness.setLibrary(player1, List.of(untouched));
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
        harness.setLibrary(player1, List.of(untouched));

        castGrizzlyBears();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
    }

    @Test
    void cascadeIncludesChosenXInSpellManaValue() {
        setupCasterTurn();
        harness.addToBattlefield(player1, new MaelstromNexus());
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(hit));
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
    }

    @Test
    void nexusWithoutAbilitiesDoesNotGrantCascade() {
        setupCasterTurn();
        harness.addToBattlefieldAndReturn(player1, new MaelstromNexus())
                .setLosesAllAbilitiesUntilEndOfTurn(true);
        LlanowarElves untouched = new LlanowarElves();
        harness.setLibrary(player1, List.of(untouched));

        castGrizzlyBears();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
    }

    @Test
    void multipleNexusesGrantSeparateCascadeTriggers() {
        setupCasterTurn();
        harness.addToBattlefield(player1, new MaelstromNexus());
        harness.addToBattlefield(player1, new MaelstromNexus());
        LlanowarElves first = new LlanowarElves();
        LlanowarElves second = new LlanowarElves();
        harness.setLibrary(player1, List.of(first, second));

        castGrizzlyBears();
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(first);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(second);
    }

    @Test
    void spellCastBeforeNexusEnteredStillCountsAsFirstSpell() {
        setupCasterTurn();
        castGrizzlyBears();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new MaelstromNexus());
        LlanowarElves untouched = new LlanowarElves();
        harness.setLibrary(player1, List.of(untouched));

        castGrizzlyBears();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
    }

    @Test
    void opponentDoesNotReceiveCascadeFromYourNexus() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new MaelstromNexus());
        LlanowarElves untouched = new LlanowarElves();
        harness.setLibrary(player2, List.of(untouched));

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(untouched);
    }

    @Test
    void cascadeHitIsCastForFreeWithoutReceivingAnotherCascade() {
        setupCasterTurn();
        harness.addToBattlefield(player1, new MaelstromNexus());
        Forest skipped = new Forest();
        LlanowarElves hit = new LlanowarElves();
        harness.setLibrary(player1, List.of(skipped, hit));

        castGrizzlyBears();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(skipped);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void firstSpellOnOpponentsTurnAlsoGetsCascade() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new MaelstromNexus());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void existingCascadeAndGrantedCascadeBothTrigger() {
        setupCasterTurn();
        harness.addToBattlefield(player1, new MaelstromNexus());
        GrizzlyBears first = new GrizzlyBears();
        LlanowarElves second = new LlanowarElves();
        harness.setLibrary(player1, List.of(first, second));

        harness.castFromHand(player1, new BloodbraidElf(), "{2}{R}{G}");
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(first);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(second);
    }

    private void setupCasterTurn() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
    }

    private void castGrizzlyBears() {
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
    }
}
