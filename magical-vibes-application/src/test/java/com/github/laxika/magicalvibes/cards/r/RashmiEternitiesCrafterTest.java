package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CatharticReunion;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RashmiEternitiesCrafter.class, Forest.class, GrizzlyBears.class, LlanowarElves.class,
        CatharticReunion.class, RuleOfLaw.class})
class RashmiEternitiesCrafterTest extends BaseCardTest {

    @Test
    @DisplayName("The first spell offers an eligible top card for free")
    void firstSpellOffersEligibleTopCard() {
        setupRashmi();
        Card top = new LlanowarElves();
        setLibraryTop(top);
        castGrizzlyBears();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
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
        harness.handleMayAbilityChosen(player1, false);

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

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
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
    void emptyLibraryDoesNotDrawOrOfferACast() {
        setupRashmi();
        harness.setLibrary(player1, List.of());
        castGrizzlyBears();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
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
    void cannotCastRevealedSpellWhenMandatoryDiscardCostCannotBePaid() {
        setupRashmi();
        Card top = new CatharticReunion();
        setLibraryTop(top);
        harness.setHand(player1, List.of(new RashmiEternitiesCrafter()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == top);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top);
    }

    @Test
    void spellLimitPreventsFreeCastAndPutsRevealedCardIntoHand() {
        setupRashmi();
        harness.addToBattlefield(player2, new RuleOfLaw());
        Card top = new LlanowarElves();
        setLibraryTop(top);
        castGrizzlyBears();

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == top);
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
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
