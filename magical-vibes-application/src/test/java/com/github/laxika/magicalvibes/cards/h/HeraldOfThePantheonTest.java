package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeraldOfThePantheonTest extends BaseCardTest {

    @Test
    @DisplayName("Enchantment spells you cast cost {1} less")
    void enchantmentSpellsCostOneLess() {
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        // Ghostly Prison costs {2}{W} — with the {1} reduction it costs {1}{W}
        harness.setHand(player1, List.of(new GhostlyPrison()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Ghostly Prison"));
    }

    @Test
    @DisplayName("Without the Herald the same enchantment is unaffordable")
    void noReductionWithoutHerald() {
        harness.setHand(player1, List.of(new GhostlyPrison()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Nonenchantment spells are not reduced")
    void creatureSpellsNotReduced() {
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        // Grizzly Bears costs {1}{G}; a single {G} is not enough
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponents' enchantment spells are not reduced")
    void opponentEnchantmentsNotReduced() {
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        harness.setHand(player2, List.of(new GhostlyPrison()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gains 1 life whenever you cast an enchantment spell")
    void gainsLifeOnEnchantmentCast() {
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        harness.setHand(player1, List.of(new GhostlyPrison()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        int startingLife = gd.getLife(player1.getId());

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("Casting a creature spell gains no life")
    void noLifeOnCreatureCast() {
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int startingLife = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Opponent casting an enchantment gains its controller no life")
    void noLifeOnOpponentEnchantmentCast() {
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        harness.setHand(player2, List.of(new GhostlyPrison()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        int startingLife = gd.getLife(player1.getId());

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
    }
}
