package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Windstorm.class, WindDrake.class, RuneclawBear.class})
class WindstormTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Windstorm puts it on the stack as an instant spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Windstorm()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, 3, null);

        GameData gd = harness.getGameData();

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(Windstorm.class);
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deals X damage to creatures with flying")
    void dealsXDamageToFlyingCreatures() {
        harness.addToBattlefield(player1, new WindDrake());
        harness.addToBattlefield(player2, new WindDrake());

        harness.setHand(player1, List.of(new Windstorm()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, 2, null);

        harness.passBothPriorities();

        // Both flying creatures should be destroyed (2 damage >= 2 toughness)
        harness.assertNotOnBattlefield(player1, "Wind Drake");
        harness.assertNotOnBattlefield(player2, "Wind Drake");
    }

    @Test
    @DisplayName("Does not damage non-flying creatures")
    void doesNotDamageNonFlyingCreatures() {
        harness.addToBattlefield(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new Windstorm()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, 3, null);

        harness.passBothPriorities();

        // Non-flying creature survives
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Does not damage players")
    void doesNotDamagePlayers() {
        harness.setHand(player1, List.of(new Windstorm()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, 3, null);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("X=0 deals no damage")
    void xZeroDealsNoDamage() {
        harness.addToBattlefield(player2, new WindDrake());

        harness.setHand(player1, List.of(new Windstorm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, 0, null);

        harness.passBothPriorities();

        // Flying creature survives with 0 damage
        harness.assertOnBattlefield(player2, "Wind Drake");
    }

    @Test
    @DisplayName("Nonlethal damage is marked only on flying creatures on both sides")
    void marksNonlethalDamageOnlyOnFlyingCreatures() {
        Permanent ownFlyer = harness.addToBattlefieldAndReturn(player1, new WindDrake());
        Permanent opposingFlyer = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new Windstorm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wind Drake");
        harness.assertOnBattlefield(player2, "Wind Drake");
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        assertThat(ownFlyer.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingFlyer.getMarkedDamage()).isEqualTo(1);
        assertThat(groundCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Windstorm");
    }
}
