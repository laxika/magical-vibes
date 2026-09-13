package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarksteelSentinel;
import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        BloodfireColossus.class,
        GrizzlyBears.class,
        EnormousBaloth.class,
        DarksteelSentinel.class,
        GloriousAnthem.class
})
class BloodfireColossusTest extends BaseCardTest {

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
        assertThat(gd.stack.getFirst().getCard()).isSameAs(colossus);
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
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 14);
    }

    // ===== Damage to creatures =====

    @Test
    @DisplayName("Destroys creatures with toughness 6 or less")
    void destroysCreaturesWithToughness6OrLess() {
        BloodfireColossus colossus = new BloodfireColossus();
        harness.addToBattlefield(player1, colossus);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new BloodfireColossus());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Both Grizzly Bears (2/2) should be destroyed
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Bloodfire Colossus");

        // Both should be in their owners' graveyards
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Bloodfire Colossus");
    }

    @Test
    @DisplayName("Creatures with toughness greater than 6 survive")
    void creaturesWithHighToughnessSurvive() {
        BloodfireColossus colossus = new BloodfireColossus();
        harness.addToBattlefield(player1, colossus);
        harness.addToBattlefield(player2, new EnormousBaloth());
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
        harness.addToBattlefield(player2, new DarksteelSentinel());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Indestructible 3/3 survives even though 6 damage would be lethal
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

        harness.assertLife(player1, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    // ===== Does not affect non-creature permanents =====

    @Test
    @DisplayName("Non-creature permanents are not affected by the damage")
    void nonCreaturePermanentsUnaffected() {
        BloodfireColossus colossus = new BloodfireColossus();
        harness.addToBattlefield(player1, colossus);

        harness.addToBattlefield(player2, new GloriousAnthem());

        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Enchantment should still be on the battlefield
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }
}

