package com.github.laxika.magicalvibes.cards.u;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UndyingFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles until a nonland card and deals damage equal to its mana value")
    void exilesUntilNonlandAndDealsManaValueDamage() {
        Card forest = new Forest();
        Card divination = new Divination();
        harness.setLibrary(player1, List.of(forest, divination));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new UndyingFlames()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(forest, divination);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Exiles the whole library and deals no damage when it has no nonland card")
    void noNonlandCardDealsNoDamage() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new UndyingFlames()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(forest);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Applies Epic and copies the effect at the controller's upkeep")
    void appliesEpicAndCopiesAtUpkeep() {
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new UndyingFlames()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playersCantCastSpellsForRestOfGame).contains(player1.getId());
        Card upkeepForest = new Forest();
        Card upkeepShock = new Shock();
        harness.setLibrary(player1, List.of(upkeepForest, upkeepShock));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock, upkeepForest, upkeepShock);
    }

    @Test
    @DisplayName("Prevents the controller from casting spells after Epic resolves")
    void preventsControllerFromCastingSpells() {
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new UndyingFlames()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
