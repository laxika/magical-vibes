package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(top);
        gd.playerDecks.get(player1.getId()).add(new Forest());
    }
}
