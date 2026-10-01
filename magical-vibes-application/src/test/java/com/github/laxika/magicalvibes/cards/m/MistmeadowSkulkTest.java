package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BladeOfTheSixthPride;
import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.f.FatalAttraction;
import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.cards.s.SpinIntoMyth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistmeadowSkulk.class, BlindPhantasm.class, BladeOfTheSixthPride.class,
        FomoriNomad.class, SpinIntoMyth.class, FatalAttraction.class})
class MistmeadowSkulkTest extends BaseCardTest {

    @Test
    @DisplayName("Creature with mana value 3 cannot block Mistmeadow Skulk")
    void manaValue3CannotBlock() {
        Permanent attacker = addCreatureReady(player1, new MistmeadowSkulk());
        attacker.setAttacking(true);

        addCreatureReady(player2, new BlindPhantasm());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Creature with mana value 2 can block Mistmeadow Skulk")
    void manaValue2CanBlock() {
        Permanent attacker = addCreatureReady(player1, new MistmeadowSkulk());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new BladeOfTheSixthPride());

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Mistmeadow Skulk takes no combat damage from a mana value 3 or greater creature")
    void takesNoDamageFromHighManaValueCreature() {
        Permanent attacker = addCreatureReady(player1, new FomoriNomad());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new MistmeadowSkulk());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);

        // The 4/4's damage to the 1/1 Skulk is prevented (protection from MV 3+); Skulk survives.
        harness.assertOnBattlefield(player2, "Mistmeadow Skulk");
    }

    @Test
    @DisplayName("A spell with mana value 3 or greater cannot target Mistmeadow Skulk")
    void highManaValueSpellCannotTarget() {
        Permanent skulk = addCreatureReady(player2, new MistmeadowSkulk());

        harness.setHand(player1, List.of(new SpinIntoMyth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, skulk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("An Aura with mana value 3 or greater cannot enchant Mistmeadow Skulk")
    void highManaValueAuraCannotEnchant() {
        Permanent skulk = addCreatureReady(player2, new MistmeadowSkulk());

        harness.setHand(player1, List.of(new FatalAttraction()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, skulk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Combat damage from Mistmeadow Skulk gains its controller that much life")
    void gainsLifeFromCombatDamage() {
        harness.setLife(player1, 10);
        addCreatureReady(player1, new MistmeadowSkulk());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
    }
}
