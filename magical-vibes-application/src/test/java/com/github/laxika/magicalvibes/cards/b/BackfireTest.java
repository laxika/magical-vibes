package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Backfire.class, GrizzlyBears.class, HowlingMine.class, ProdigalSorcerer.class})
class BackfireTest extends BaseCardTest {

    /** Enchants the opponent's creature with Backfire (controlled by player1). */
    private Permanent enchantOpponentCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Backfire()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        return bears;
    }

    @Test
    @DisplayName("When the enchanted creature deals combat damage to you, that much is dealt to its controller")
    void reflectsCombatDamageToItsController() {
        enchantOpponentCreature();

        int creatureControllerLifeBefore = gd.playerLifeTotals.get(player2.getId());

        // player2 attacks player1 (the aura's controller) with the enchanted 2/2.
        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        // 2 combat damage dealt to player1 → Backfire deals 2 to player2 (the creature's controller).
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(creatureControllerLifeBefore - 2);
    }

    @Test
    @DisplayName("No reflection when the enchanted creature deals no damage to you")
    void noReflectionWithoutDamage() {
        enchantOpponentCreature();

        int creatureControllerLifeBefore = gd.playerLifeTotals.get(player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(creatureControllerLifeBefore);
    }

    @Test
    @DisplayName("Reflects noncombat damage and uses Backfire as the reflected damage source")
    void reflectsNoncombatDamageFromTheAura() {
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());

        harness.setHand(player1, List.of(new Backfire()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, sorcerer.getId());
        harness.passBothPriorities();

        int auraControllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int creatureControllerLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(auraControllerLifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(creatureControllerLifeBefore - 1);
        assertThat(gameLogContains("damage from Backfire")).isTrue();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // a legal creature target must exist
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        harness.setHand(player1, List.of(new Backfire()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
