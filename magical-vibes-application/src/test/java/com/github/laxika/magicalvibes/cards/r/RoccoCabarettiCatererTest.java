package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoccoCabarettiCaterer.class, GrizzlyBears.class, HillGiant.class, Plains.class})
class RoccoCabarettiCatererTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, Rocco offers to search for a creature with mana value at most X")
    void castTriggersBoundedCreatureSearch() {
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setLibrary(player1, List.of(eligible, tooExpensive, new Plains()));

        castRocco(2);
        resolveRoccoEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(eligible);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == eligible);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(eligible);
    }

    @Test
    @DisplayName("Declining the search leaves the library and battlefield unchanged")
    void decliningSearchDoesNothing() {
        Card eligible = new GrizzlyBears();
        harness.setLibrary(player1, List.of(eligible));

        castRocco(2);
        resolveRoccoEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == eligible);
    }

    @Test
    @DisplayName("Putting Rocco onto the battlefield without casting it does not trigger the search")
    void enteringWithoutBeingCastDoesNothing() {
        Card eligible = new GrizzlyBears();
        harness.setLibrary(player1, List.of(eligible));

        harness.enterBattlefieldAndReturn(player1, new RoccoCabarettiCaterer());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligible);
    }

    private void castRocco(int xValue) {
        harness.setHand(player1, List.of(new RoccoCabarettiCaterer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        gs.playCard(gd, player1, 0, xValue, null, null);
    }

    private void resolveRoccoEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
