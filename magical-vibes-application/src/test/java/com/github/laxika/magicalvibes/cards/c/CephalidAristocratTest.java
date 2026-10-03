package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelOfRetribution;
import com.github.laxika.magicalvibes.cards.c.CabalTorturer;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CephalidAristocrat.class, AngelOfRetribution.class, CabalTorturer.class, FieryTemper.class})
class CephalidAristocratTest extends BaseCardTest {

    @Test
    @DisplayName("Mills two cards when targeted by a spell")
    void millsWhenTargetedBySpell() {
        Permanent aristocrat = harness.addToBattlefieldAndReturn(player1, new CephalidAristocrat());
        harness.setLibrary(player1, List.of(new AngelOfRetribution(), new AngelOfRetribution()));

        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, aristocrat.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Angel of Retribution", "Angel of Retribution");
    }

    @Test
    @DisplayName("Mills two cards when targeted by an ability")
    void millsWhenTargetedByAbility() {
        Permanent aristocrat = harness.addToBattlefieldAndReturn(player1, new CephalidAristocrat());
        harness.setLibrary(player1, List.of(new AngelOfRetribution(), new AngelOfRetribution()));
        addCreatureReady(player2, new CabalTorturer());
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbility(player2, 0, null, aristocrat.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Angel of Retribution", "Angel of Retribution");
    }

    @Test
    @DisplayName("Mills two cards when targeted by its controller's spell")
    void millsWhenTargetedByControllersSpell() {
        Permanent aristocrat = harness.addToBattlefieldAndReturn(player1, new CephalidAristocrat());
        harness.setLibrary(player1, List.of(new AngelOfRetribution(), new AngelOfRetribution()));

        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, aristocrat.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Angel of Retribution", "Angel of Retribution");
    }

    @Test
    @DisplayName("Mills only the cards available in a short library")
    void millsOnlyAvailableCardsInShortLibrary() {
        Permanent aristocrat = harness.addToBattlefieldAndReturn(player1, new CephalidAristocrat());
        harness.setLibrary(player1, List.of(new AngelOfRetribution()));

        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, aristocrat.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Angel of Retribution");
    }

    @Test
    @DisplayName("Each targeting spell mills two cards even in the same turn")
    void millsForEachTargetingSpell() {
        Permanent aristocrat = harness.addToBattlefieldAndReturn(player1, new CephalidAristocrat());
        harness.setLibrary(player1, List.of(
                new AngelOfRetribution(), new AngelOfRetribution(),
                new AngelOfRetribution(), new AngelOfRetribution()));
        harness.setHand(player2, List.of(new FieryTemper(), new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castAndResolveInstant(player2, 0, aristocrat.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.castAndResolveInstant(player2, 0, aristocrat.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Angel of Retribution", "Angel of Retribution",
                        "Angel of Retribution", "Angel of Retribution");
        harness.assertOnBattlefield(player1, "Cephalid Aristocrat");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Targeting another creature does not trigger milling")
    void doesNotMillWhenAnotherCreatureIsTargeted() {
        harness.addToBattlefield(player1, new CephalidAristocrat());
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new AngelOfRetribution());
        harness.setLibrary(player1, List.of(new AngelOfRetribution(), new AngelOfRetribution()));
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, angel.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Fiery Temper");
    }

    @Test
    @DisplayName("Milling an empty library does not cause a loss")
    void millsEmptyLibraryWithoutLosing() {
        Permanent aristocrat = harness.addToBattlefieldAndReturn(player1, new CephalidAristocrat());
        harness.setLibrary(player1, List.of());
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, aristocrat.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertOnBattlefield(player1, "Cephalid Aristocrat");
    }
}
