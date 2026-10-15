package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.w.WoodlandChasm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeverwinterDryad.class, Forest.class, SnowCoveredForest.class, Plains.class, WoodlandChasm.class})
class NeverwinterDryadTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and searches for a basic Forest onto the battlefield tapped")
    void sacrificesAndSearchesForBasicForest() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new NeverwinterDryad());
        Forest forest = new Forest();
        SnowCoveredForest snowCoveredForest = new SnowCoveredForest();
        Plains plains = new Plains();
        NeverwinterDryad creature = new NeverwinterDryad();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(forest, snowCoveredForest, plains, creature));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(dryad.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(dryad.getCard().getId());

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(forest.getId(), snowCoveredForest.getId());

        Card chosen = search.params().cards().getFirst();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(chosen.getId()) && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(List.of(forest, snowCoveredForest, plains, creature).stream()
                        .filter(card -> card != chosen).map(Card::getId).toList());
    }

    @Test
    @DisplayName("Can fail to find even when a basic Forest is available")
    void canFailToFind() {
        harness.addToBattlefield(player1, new NeverwinterDryad());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Neverwinter Dryad");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolves without finding a card when the library has no basic Forest")
    void noMatchingCard() {
        harness.addToBattlefield(player1, new NeverwinterDryad());
        Plains plains = new Plains();
        NeverwinterDryad creature = new NeverwinterDryad();
        WoodlandChasm nonbasicForest = new WoodlandChasm();
        harness.setLibrary(player1, List.of(plains, creature, nonbasicForest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Neverwinter Dryad");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, creature, nonbasicForest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new NeverwinterDryad());
        dryad.tap();
        dryad.setSummoningSick(true);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Neverwinter Dryad");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(forest.getId());
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot sacrifice the Dryad without paying two mana")
    void insufficientManaDoesNotSacrifice() {
        harness.addToBattlefield(player1, new NeverwinterDryad());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Neverwinter Dryad");
        harness.assertNotInGraveyard(player1, "Neverwinter Dryad");
        assertThat(gd.stack).isEmpty();
    }
}
