package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CateranBrute.class, CateranKidnappers.class, CateranPersuader.class,
        CateranSummons.class, Swamp.class})
class CateranBruteTest extends BaseCardTest {

    @Test
    void searchesForMercenaryPermanentWithManaValueAtMostTwo() {
        Permanent brute = addCreatureReady(player1, new CateranBrute());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(
                new CateranPersuader(), new Swamp(), new CateranSummons(), new CateranKidnappers()));

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(brute.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Cateran Persuader");

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Cateran Persuader");
        harness.assertNotOnBattlefield(player1, "Swamp");
        harness.assertNotOnBattlefield(player1, "Cateran Summons");
        harness.assertNotOnBattlefield(player1, "Cateran Kidnappers");
    }

    @Test
    void doesNotOpenSearchWhenLibraryHasNoEligibleMercenaryPermanent() {
        Permanent brute = addCreatureReady(player1, new CateranBrute());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Swamp(), new CateranSummons(), new CateranKidnappers()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(brute.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
