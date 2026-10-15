package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void canFailToFindEvenWhenAnEligibleMercenaryIsInTheLibrary() {
        addCreatureReady(player1, new CateranKidnappers());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(new CateranBrute()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Cateran Brute");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Cateran Brute");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void putsTheChosenMercenaryOntoItsControllersBattlefieldUntapped() {
        addCreatureReady(player2, new CateranKidnappers());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(new CateranBrute()));
        harness.setLibrary(player2, List.of(new CateranPersuader()));

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(findPermanent(player2, "Cateran Persuader").isTapped()).isFalse();
        assertThat(findPermanent(player2, "Cateran Persuader").isSummoningSick()).isTrue();
        harness.assertNotOnBattlefield(player1, "Cateran Persuader");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Cateran Brute");
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        addCreatureReady(player1, new CateranKidnappers());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateWithoutThreeMana() {
        addCreatureReady(player1, new CateranKidnappers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "Cateran Kidnappers").isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new CateranKidnappers());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "Cateran Kidnappers").isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
