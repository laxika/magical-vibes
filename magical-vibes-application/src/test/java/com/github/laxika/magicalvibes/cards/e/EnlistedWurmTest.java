package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnlistedWurm.class, AvatarOfMight.class, Forest.class, HillGiant.class,
        LlanowarElves.class, Mountain.class, Plains.class})
class EnlistedWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Cascade skips lands and equal/greater-cost nonlands, stopping at the first lesser one")
    void cascadeDigsToFirstLesserNonland() {
        setupCasterTurn();

        // Enlisted Wurm is {4}{G}{W} = mana value 6. Dig should skip the land and the MV-8 Avatar of
        // Might (greater, not less), stop at Hill Giant (MV 4 < 6), and never touch the Elves beneath it.
        LlanowarElves belowHit = new LlanowarElves();
        harness.setLibrary(player1, List.of(
                new Mountain(), new AvatarOfMight(), new HillGiant(), belowHit));

        castEnlistedWurm();
        harness.passBothPriorities(); // resolve the cascade trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Hill Giant");

        // The dig stopped at the hit — the card beneath it stays on the library.
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowHit);
    }

    @Test
    @DisplayName("Casting the cascade hit puts it on the stack for free; the rest go to the bottom")
    void castingHitPutsItOnStack() {
        setupCasterTurn();

        LlanowarElves belowHit = new LlanowarElves();
        Mountain land = new Mountain();
        AvatarOfMight skipped = new AvatarOfMight();
        harness.setLibrary(player1, List.of(land, skipped, new HillGiant(), belowHit));

        castEnlistedWurm();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Hill Giant")
                && se.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(belowHit, land, skipped);
    }

    @Test
    @DisplayName("Cascade with no qualifying nonland bottoms everything and prompts nothing")
    void noQualifyingCardBottomsEverything() {
        setupCasterTurn();

        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new Plains()));

        castEnlistedWurm();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Declining cascade bottoms the hit and skipped cards beneath the untouched library")
    void decliningHitReturnsAllExiledCardsToBottom() {
        setupCasterTurn();
        Mountain land = new Mountain();
        HillGiant hit = new HillGiant();
        LlanowarElves belowHit = new LlanowarElves();
        harness.setLibrary(player1, List.of(land, hit, belowHit));

        castEnlistedWurm();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(belowHit);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(land, hit);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == hit);
        assertThat(gd.exiledCards).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Enlisted Wurm");
    }

    @Test
    @DisplayName("Cascade skips another Enlisted Wurm with equal mana value")
    void cascadeSkipsEqualManaValue() {
        setupCasterTurn();
        EnlistedWurm equalCost = new EnlistedWurm();
        HillGiant hit = new HillGiant();
        harness.setLibrary(player1, List.of(equalCost, hit));

        castEnlistedWurm();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(equalCost, hit);
    }

    @Test
    @DisplayName("Cascade with an empty library still lets Enlisted Wurm resolve")
    void emptyLibraryDoesNotPreventWurmResolving() {
        setupCasterTurn();
        harness.setLibrary(player1, List.of());

        castEnlistedWurm();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Enlisted Wurm");
    }

    @Test
    @DisplayName("The free creature resolves before Enlisted Wurm and leaves skipped cards at the bottom")
    void cascadeHitResolvesBeforeWurm() {
        setupCasterTurn();
        Mountain land = new Mountain();
        HillGiant hit = new HillGiant();
        LlanowarElves belowHit = new LlanowarElves();
        harness.setLibrary(player1, List.of(land, hit, belowHit));

        castEnlistedWurm();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowHit, land);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Enlisted Wurm");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Enlisted Wurm");
    }

    @Test
    @DisplayName("Cards passed over by cascade and the hit are exiled face up during the casting choice")
    void cascadeCardsAreInExileDuringChoice() {
        setupCasterTurn();
        Mountain land = new Mountain();
        AvatarOfMight skipped = new AvatarOfMight();
        HillGiant hit = new HillGiant();
        harness.setLibrary(player1, List.of(land, skipped, hit));

        castEnlistedWurm();
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .containsExactlyInAnyOrder(land, skipped, hit);
        assertThat(gd.exiledCards).allMatch(entry -> !entry.faceDown());
    }

    @Test
    @DisplayName("The cascade hit is cast from exile rather than the graveyard")
    void cascadeHitIsRecordedAsCastFromExile() {
        setupCasterTurn();
        harness.setLibrary(player1, List.of(new HillGiant()));

        castEnlistedWurm();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getSpellsCastThisTurnCount(player1.getId(), Zone.EXILE)).isEqualTo(1);
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId(), Zone.GRAVEYARD)).isZero();
    }

    private void setupCasterTurn() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
    }

    private void castEnlistedWurm() {
        harness.castFromHand(player1, new EnlistedWurm(), "{4}{G}{W}");
    }
}
