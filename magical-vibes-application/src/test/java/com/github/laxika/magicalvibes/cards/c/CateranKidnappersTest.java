package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CateranKidnappers.class, CateranBrute.class, CateranPersuader.class,
        CacklingWitch.class, CateranEnforcer.class})
class CateranKidnappersTest extends BaseCardTest {

    @Test
    void searchesForMercenaryPermanentWithManaValueAtMostThree() {
        addCreatureReady(player1, new CateranKidnappers());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setLibrary(player1, List.of(
                new CateranBrute(), new CateranPersuader(), new CacklingWitch(), new CateranEnforcer()));

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(findPermanent(player1, "Cateran Kidnappers").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Cateran Brute", "Cateran Persuader");

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Cateran Brute");
        harness.assertNotOnBattlefield(player1, "Cateran Persuader");
        harness.assertNotOnBattlefield(player1, "Cackling Witch");
        harness.assertNotOnBattlefield(player1, "Cateran Enforcer");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Cateran Persuader", "Cackling Witch", "Cateran Enforcer");
    }

    @Test
    void resolvesWithoutInteractionWhenLibraryHasNoEligibleMercenaryPermanent() {
        addCreatureReady(player1, new CateranKidnappers());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(new CacklingWitch(), new CateranEnforcer()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Cackling Witch", "Cateran Enforcer");
    }
}
