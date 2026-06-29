package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GainLifePerCreatureOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BluntTheAssaultTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Blunt the Assault has correct effects")
    void hasCorrectEffects() {
        BluntTheAssault card = new BluntTheAssault();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(GainLifePerCreatureOnBattlefieldEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(PreventAllCombatDamageEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Blunt the Assault puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new BluntTheAssault()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Blunt the Assault");
    }

    // ===== Life gain =====

    @Test
    @DisplayName("Gains 1 life for each creature on the battlefield")
    void gainsLifePerCreature() {
        // Put 2 creatures on player1's battlefield, 1 on player2's = 3 total
        addCreature(player1);
        addCreature(player1);
        addCreature(player2);

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BluntTheAssault()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Gains no life when no creatures on the battlefield")
    void gainsNoLifeWhenNoCreatures() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BluntTheAssault()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Combat damage prevention =====

    @Test
    @DisplayName("Prevents all combat damage after resolving")
    void preventsAllCombatDamage() {
        harness.setHand(player1, List.of(new BluntTheAssault()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("Both effects resolve together: life gain and damage prevention")
    void bothEffectsResolveTogether() {
        // 3 creatures total on the battlefield
        Permanent attacker1 = addCreature(player2);
        attacker1.setAttacking(true);
        Permanent attacker2 = addCreature(player2);
        attacker2.setAttacking(true);
        addCreature(player1);

        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new BluntTheAssault()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Should gain 3 life (3 creatures on battlefield)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        // Should prevent all combat damage
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    // ===== Helper methods =====

    private Permanent addCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
