package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CatharticReunion;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.w.WearTear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RashmiEternitiesCrafter.class, Cancel.class, Forest.class, GrizzlyBears.class, LlanowarElves.class,
        CatharticReunion.class, RuleOfLaw.class, WearTear.class, AngelicChorus.class})
class RashmiEternitiesCrafterTest extends BaseCardTest {

    @Test
    @DisplayName("The first spell offers an eligible top card for free")
    void firstSpellOffersEligibleTopCard() {
        setupRashmi();
        Card top = new LlanowarElves();
        setLibraryTop(top);
        castGrizzlyBears();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Declining the free cast puts the revealed card into hand")
    void decliningFreeCastPutsCardIntoHand() {
        setupRashmi();
        Card top = new LlanowarElves();
        setLibraryTop(top);
        castGrizzlyBears();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top);
    }

    @Test
    @DisplayName("An ineligible top card goes into hand without a choice")
    void ineligibleTopCardGoesIntoHand() {
        setupRashmi();
        Card top = new GrizzlyBears();
        setLibraryTop(top);
        castGrizzlyBears();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top);
    }

    @Test
    @DisplayName("Only the first spell each turn triggers Rashmi")
    void onlyFirstSpellTriggers() {
        setupRashmi();
        Card firstTop = new Forest();
        setLibraryTop(firstTop);
        castGrizzlyBears();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        Card secondTop = new LlanowarElves();
        setLibraryTop(secondTop);
        castGrizzlyBears();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(secondTop);
    }

    @Test
    @DisplayName("An empty library offers no cast and puts nothing into the hand")
    void emptyLibraryDoesNotDrawOrOfferACast() {
        setupRashmi();
        harness.setLibrary(player1, List.of());
        castGrizzlyBears();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rashmi entering after the first spell doesn't trigger on the second")
    void enteringAfterFirstSpellDoesNotTriggerOnSecondSpell() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        castGrizzlyBears();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new RashmiEternitiesCrafter());
        Card top = new LlanowarElves();
        setLibraryTop(top);

        castGrizzlyBears();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
    }

    @Test
    @DisplayName("The trigger still resolves after Rashmi has left the battlefield")
    void triggerResolvesAfterRashmiLeavesBattlefield() {
        setupRashmi();
        Card top = new Forest();
        setLibraryTop(top);
        castGrizzlyBears();
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top);
    }

    @Test
    @DisplayName("A revealed spell whose mandatory discard can't be paid isn't cast and goes into the hand")
    void cannotCastRevealedSpellWhenMandatoryDiscardCostCannotBePaid() {
        setupRashmi();
        Card top = new CatharticReunion();
        setLibraryTop(top);
        harness.setHand(player1, List.of(new RashmiEternitiesCrafter()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class) != null) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == top);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top);
    }

    @Test
    @DisplayName("A spell limit stops the free cast and the revealed card goes into the hand")
    void spellLimitPreventsFreeCastAndPutsRevealedCardIntoHand() {
        setupRashmi();
        harness.addToBattlefield(player2, new RuleOfLaw());
        Card top = new LlanowarElves();
        setLibraryTop(top);
        castGrizzlyBears();

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class) != null) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == top);
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("A first spell countered before the ability resolves still sets the mana value")
    void counteredSpellsManaValueIsUsedAsItLastExistedOnTheStack() {
        setupRashmi();
        Card top = new LlanowarElves();
        setLibraryTop(top);
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("A split card's half with lesser mana value may be cast though the card's mana value isn't lesser")
    void splitCardHalfWithLesserManaValueMayBeCast() {
        setupRashmi();
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID chorusId = harness.getPermanentId(player2, "Angelic Chorus");
        // Grizzly Bears has mana value 2: Wear // Tear's is 3, Wear's 2 and Tear's 1 (CR 709.3a, 709.4).
        WearTear wearTear = new WearTear();
        setLibraryTop(wearTear);
        castGrizzlyBears();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Tear — Destroy target enchantment");
        harness.handleListChoice(player1, "Tear — Destroy target enchantment");
        harness.handlePermanentChosen(player1, chorusId);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(wearTear);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("The revealed card goes into the hand after the cast is declined, as the ability's last step")
    void revealedCardNotCastGoesIntoHandAfterTheChoice() {
        setupRashmi();
        Card top = new LlanowarElves();
        setLibraryTop(top);
        castGrizzlyBears();

        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
        assertThat(gameLogContains("reveals Llanowar Elves")).isTrue();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top);
    }

    private void setupRashmi() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new RashmiEternitiesCrafter());
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }

    private void setLibraryTop(Card top) {
        harness.setLibrary(player1, List.of(top, new Forest()));
    }
}
