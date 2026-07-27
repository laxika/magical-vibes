package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloodfireColossusTest extends BaseCardTest {

    /** A 7/7 creature that survives 6 damage. */
    private static Card toughCreature() {
        Card card = new Card();
        card.setName("Enormous Baloth");
        card.setType(CardType.CREATURE);
        card.setManaCost("{6}{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(7);
        card.setToughness(7);
        return card;
    }

    /** A 2/2 indestructible creature. */
    private static Card indestructibleCreature() {
        Card card = new Card();
        card.setName("Darksteel Sentinel");
        card.setType(CardType.CREATURE);
        card.setManaCost("{6}");
        card.setColor(CardColor.WHITE);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        return card;
    }

    // ===== Activation and sacrifice =====

    @Test
    @DisplayName("Activating ability sacrifices Bloodfire Colossus and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        BloodfireColossus colossus = new BloodfireColossus();
        harness.addToBattlefield(player1, colossus);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();

        // Colossus should be sacrificed
        harness.assertNotOnBattlefield(player1, "Bloodfire Colossus");
        harness.assertInGraveyard(player1, "Bloodfire Colossus");

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bloodfire Colossus");
    }

    // ===== Damage to all players =====

    @Test
    @DisplayName("Deals 6 damage to each player")
    void deals6DamageToEachPlayer() {
        BloodfireColossus colossus = new BloodfireColossus();
        harness.addToBattlefield(player1, colossus);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14); // 20 - 6
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14); // 20 - 6
    }

    // ===== Damage to creatures =====

    @Test
    @DisplayName("Destroys creatures with toughness 6 or less")
    void destroysCreaturesWithToughness6OrLess() {
        BloodfireColossus colossus = new BloodfireColossus();
        harness.addToBattlefield(player1, colossus);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Both Grizzly Bears (2/2) should be destroyed
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        // Both should be in their owners' graveyards
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creatures with toughness greater than 6 survive")
    void creaturesWithHighToughnessSurvive() {
        BloodfireColossus colossus = new BloodfireColossus();
        harness.addToBattlefield(player1, colossus);
        harness.addToBattlefield(player2, toughCreature());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // 7/7 survives 6 damage
        harness.assertOnBattlefield(player2, "Enormous Baloth");
    }

    // ===== Indestructible =====

    @Test
    @DisplayName("Indestructible creatures survive the damage")
    void indestructibleCreaturesSurvive() {
        BloodfireColossus colossus = new BloodfireColossus();
        harness.addToBattlefield(player1, colossus);
        harness.addToBattlefield(player2, indestructibleCreature());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Indestructible 2/2 survives even though 6 >= 2
        harness.assertOnBattlefield(player2, "Darksteel Sentinel");
    }

    // ===== Mana cost validation =====

    @Test
    @DisplayName("Cannot activate ability without {R} mana")
    void cannotActivateWithoutRedMana() {
        BloodfireColossus colossus = new BloodfireColossus();
        harness.addToBattlefield(player1, colossus);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Can kill the controller =====

    @Test
    @DisplayName("Can kill the controller when their life is 6 or less")
    void canKillController() {
        harness.setLife(player1, 6);
        BloodfireColossus colossus = new BloodfireColossus();
        harness.addToBattlefield(player1, colossus);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    // ===== Does not affect non-creature permanents =====

    @Test
    @DisplayName("Non-creature permanents are not affected by the damage")
    void nonCreaturePermanentsUnaffected() {
        BloodfireColossus colossus = new BloodfireColossus();
        harness.addToBattlefield(player1, colossus);

        Card enchantment = new Card();
        enchantment.setName("Test Enchantment");
        enchantment.setType(CardType.ENCHANTMENT);
        enchantment.setManaCost("{1}{W}");
        enchantment.setColor(CardColor.WHITE);
        harness.addToBattlefield(player2, enchantment);

        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Enchantment should still be on the battlefield
        harness.assertOnBattlefield(player2, "Test Enchantment");
    }
}

