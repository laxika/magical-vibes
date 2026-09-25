package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.cards.l.LightningDart;
import com.github.laxika.magicalvibes.cards.m.ManiacalRage;
import com.github.laxika.magicalvibes.cards.r.Repulse;
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

@CardUsed({GalinasKnight.class, AncientKavu.class, LightningDart.class, ManiacalRage.class, Repulse.class})
class GalinasKnightTest extends BaseCardTest {

    @Test
    @DisplayName("A red spell cannot target Galina's Knight")
    void redSpellCannotTargetKnight() {
        Permanent knight = addCreatureReady(player2, new GalinasKnight());

        harness.setHand(player1, List.of(new LightningDart()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("A blue spell can target Galina's Knight")
    void blueSpellCanTargetKnight() {
        Permanent knight = addCreatureReady(player2, new GalinasKnight());

        harness.setHand(player1, List.of(new Repulse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, knight.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A red creature cannot block Galina's Knight")
    void redCreatureCannotBlockKnight() {
        addCreatureReady(player1, new GalinasKnight());
        addCreatureReady(player2, new AncientKavu());
        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from red prevents combat damage from a red creature")
    void preventsCombatDamageFromRedCreature() {
        Permanent attacker = addCreatureReady(player1, new AncientKavu());
        attacker.setAttacking(true);
        Permanent knight = addCreatureReady(player2, new GalinasKnight());
        knight.setBlocking(true);
        knight.addBlockingTarget(0);
        resolveCombat();

        assertThat(knight.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(knight);
    }

    @Test
    @DisplayName("A red Aura cannot enchant Galina's Knight")
    void redAuraCannotEnchantKnight() {
        Permanent knight = addCreatureReady(player2, new GalinasKnight());

        harness.setHand(player1, List.of(new ManiacalRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }
}
