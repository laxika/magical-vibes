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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @Test
    void mayFindNothingEvenWhenAnEligibleMercenaryIsPresent() {
        addCreatureReady(player1, new CateranBrute());
        CateranPersuader persuader = new CateranPersuader();
        Swamp swamp = new Swamp();
        harness.setLibrary(player1, List.of(persuader, swamp));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Cateran Persuader");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(persuader, swamp);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void foundMercenaryEntersUntappedUnderTheActivatingPlayersControl() {
        addCreatureReady(player1, new CateranBrute());
        CateranPersuader persuader = new CateranPersuader();
        harness.setLibrary(player1, List.of(persuader));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent found = findPermanent(player1, "Cateran Persuader");
        assertThat(found.getCard()).isSameAs(persuader);
        assertThat(found.isTapped()).isFalse();
        assertThat(found.isSummoningSick()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player2, "Cateran Persuader");
        harness.assertNotInHand(player1, "Cateran Persuader");
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        Permanent brute = addCreatureReady(player1, new CateranBrute());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(brute.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        Permanent brute = addCreatureReady(player1, new CateranBrute());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(brute.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent brute = harness.addToBattlefieldAndReturn(player1, new CateranBrute());
        brute.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(brute.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
