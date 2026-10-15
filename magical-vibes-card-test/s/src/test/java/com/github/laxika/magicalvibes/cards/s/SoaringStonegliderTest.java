package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoaringStoneglider.class, Island.class, GiantGrowth.class})
class SoaringStonegliderTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles two cards from the graveyard as the additional cost")
    void exilesTwoGraveyardCards() {
        Card first = new Island();
        Card second = new GiantGrowth();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new SoaringStoneglider()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof SoaringStoneglider);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Pays the alternate mana cost when the graveyard option is not chosen")
    void paysAlternateManaCost() {
        harness.setHand(player1, List.of(new SoaringStoneglider()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof SoaringStoneglider);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot be cast when neither additional cost option can be paid")
    void rejectsWhenNeitherAdditionalCostOptionIsAvailable() {
        harness.setHand(player1, List.of(new SoaringStoneglider()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("May pay mana even when two graveyard cards are available")
    void paysManaWithoutExilingAvailableCards() {
        Card first = new Island();
        Card second = new GiantGrowth();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new SoaringStoneglider()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Soaring Stoneglider");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot exile the same graveyard card twice to pay the cost")
    void rejectsDuplicateExileSelection() {
        Card first = new Island();
        Card second = new GiantGrowth();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new SoaringStoneglider()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 0))).isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInHand(player1, "Soaring Stoneglider");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Exile cost is paid before the creature resolves")
    void paysExileCostWhileSpellIsOnStack() {
        Card first = new Island();
        Card second = new GiantGrowth();
        Card remaining = new Island();
        harness.setGraveyard(player1, List.of(first, remaining, second));
        harness.setHand(player1, List.of(new SoaringStoneglider()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 2));

        harness.assertNotOnBattlefield(player1, "Soaring Stoneglider");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Soaring Stoneglider");
    }
}
