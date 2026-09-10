package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.m.MoggRaider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EvincarsJustice.class, MoggRaider.class, HornedTurtle.class, Counterspell.class})
class EvincarsJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each creature and each player")
    void damagesEveryCreatureAndPlayer() {
        harness.addToBattlefield(player1, new MoggRaider());
        harness.addToBattlefield(player2, new MoggRaider());
        harness.setHand(player1, List.of(new EvincarsJustice()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // 1/1 Raiders take lethal damage on both sides.
        assertThat(findPermanents(player1, "Mogg Raider")).isEmpty();
        assertThat(findPermanents(player2, "Mogg Raider")).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals damage without destroying a creature that survives it")
    void marksDamageOnSurvivingCreature() {
        Permanent turtle = harness.addToBattlefieldAndReturn(player2, new HornedTurtle());
        harness.setHand(player1, List.of(new EvincarsJustice()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Horned Turtle")).hasSize(1);
        assertThat(turtle.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Without buyback the spell goes to the graveyard")
    void withoutBuybackGoesToGraveyard() {
        harness.setHand(player1, List.of(new EvincarsJustice()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(handNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).containsExactly("Evincar's Justice");
    }

    @Test
    @DisplayName("Paying buyback returns the spell to hand as it resolves")
    void buybackReturnsToHand() {
        harness.addToBattlefield(player2, new MoggRaider());
        harness.setHand(player1, List.of(new EvincarsJustice()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorceryWithBuyback(player1, 0, null);
        assertThat(gd.stack.getFirst().isBuyback()).isTrue();

        harness.passBothPriorities();

        assertThat(graveyardNames(player1)).doesNotContain("Evincar's Justice");
        assertThat(handNames(player1)).containsExactly("Evincar's Justice");
        assertThat(findPermanents(player2, "Mogg Raider")).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Buyback requires all three additional mana")
    void buybackRequiresThreeAdditionalMana() {
        EvincarsJustice justice = new EvincarsJustice();
        harness.setHand(player1, List.of(justice));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorceryWithBuyback(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(handNames(player1)).containsExactly("Evincar's Justice");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A countered buyback spell goes to its owner's graveyard")
    void counteredBuybackGoesToGraveyard() {
        EvincarsJustice justice = new EvincarsJustice();
        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(justice));
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorceryWithBuyback(player1, 0, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, justice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(handNames(player1)).doesNotContain("Evincar's Justice");
        assertThat(graveyardNames(player1)).containsExactly("Evincar's Justice");
    }

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(c -> c.getName()).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(c -> c.getName()).toList();
    }
}
