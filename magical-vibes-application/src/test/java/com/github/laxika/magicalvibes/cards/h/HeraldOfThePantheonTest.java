package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.SphinxsTutelage;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeraldOfThePantheon.class, SphinxsTutelage.class, TimberpackWolf.class})
class HeraldOfThePantheonTest extends BaseCardTest {

    @Test
    @DisplayName("Enchantment spells you cast cost {1} less")
    void enchantmentSpellsCostOneLess() {
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        // Sphinx's Tutelage costs {2}{U} — with the {1} reduction it costs {1}{U}
        harness.setHand(player1, List.of(new SphinxsTutelage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Sphinx's Tutelage"));
    }

    @Test
    @DisplayName("Without the Herald the same enchantment is unaffordable")
    void noReductionWithoutHerald() {
        harness.setHand(player1, List.of(new SphinxsTutelage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Nonenchantment spells are not reduced")
    void creatureSpellsNotReduced() {
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        // Timberpack Wolf costs {1}{G}; a single {G} is not enough
        harness.setHand(player1, List.of(new TimberpackWolf()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponents' enchantment spells are not reduced")
    void opponentEnchantmentsNotReduced() {
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        harness.setHand(player2, List.of(new SphinxsTutelage()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gains 1 life whenever you cast an enchantment spell")
    void gainsLifeOnEnchantmentCast() {
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        harness.setHand(player1, List.of(new SphinxsTutelage()));
        harness.addMana(player1, ManaColor.BLUE, 2);
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
        harness.setHand(player1, List.of(new TimberpackWolf()));
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
        harness.setHand(player2, List.of(new SphinxsTutelage()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        int startingLife = gd.getLife(player1.getId());

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Multiple Heralds stack their reductions and each gain life")
    void multipleHeraldsStack() {
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        harness.setHand(player1, List.of(new SphinxsTutelage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int startingLife = gd.getLife(player1.getId());

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 2);
        harness.assertNotOnBattlefield(player1, "Sphinx's Tutelage");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sphinx's Tutelage");
    }

    @Test
    @DisplayName("Excess generic reduction cannot pay the colored mana requirement")
    void excessReductionDoesNotRemoveColoredCost() {
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        harness.setHand(player1, List.of(new SphinxsTutelage()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Life gain resolves before the enchantment and survives the Herald leaving")
    void castTriggerSurvivesSourceLeaving() {
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        harness.setHand(player1, List.of(new SphinxsTutelage()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        int startingLife = gd.getLife(player1.getId());

        harness.castEnchantment(player1, 0);
        harness.assertLife(player1, startingLife);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 1);
        harness.assertNotOnBattlefield(player1, "Sphinx's Tutelage");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sphinx's Tutelage");
        harness.assertLife(player1, startingLife + 1);
    }

    @Test
    @DisplayName("An enchantment entering without being cast does not gain life")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.addToBattlefield(player1, new HeraldOfThePantheon());
        int startingLife = gd.getLife(player1.getId());

        harness.addToBattlefield(player1, new SphinxsTutelage());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, startingLife);
    }
}
