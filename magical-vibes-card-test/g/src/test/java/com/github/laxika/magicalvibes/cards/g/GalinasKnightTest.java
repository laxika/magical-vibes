package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.cards.b.BreathOfDarigaaz;
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

@CardUsed({GalinasKnight.class, AncientKavu.class, BreathOfDarigaaz.class,
        LightningDart.class, ManiacalRage.class, Repulse.class})
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
        declareAttackersAndPrepareBlockers(List.of(0));

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

    @Test
    @DisplayName("Protection also prevents the controller's red spell from targeting the Knight")
    void ownRedSpellCannotTargetKnight() {
        Permanent knight = addCreatureReady(player1, new GalinasKnight());
        harness.setHand(player1, List.of(new LightningDart()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Protection prevents untargeted red damage for either controller")
    void preventsUntargetedRedDamage() {
        Permanent ownKnight = addCreatureReady(player1, new GalinasKnight());
        Permanent opposingKnight = addCreatureReady(player2, new GalinasKnight());
        Permanent kavu = addCreatureReady(player2, new AncientKavu());
        harness.setHand(player1, List.of(new BreathOfDarigaaz()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(ownKnight.getMarkedDamage()).isZero();
        assertThat(opposingKnight.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownKnight);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingKnight).doesNotContain(kavu);
        harness.assertInGraveyard(player2, "Ancient Kavu");
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A Kavu that becomes colorless can block and damage Galina's Knight")
    void colorlessKavuCanBlockAndDealDamage() {
        Permanent knight = addCreatureReady(player1, new GalinasKnight());
        Permanent kavu = addCreatureReady(player2, new AncientKavu());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(knight);
        harness.assertInGraveyard(player1, "Galina's Knight");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kavu);
        assertThat(kavu.getMarkedDamage()).isEqualTo(2);
    }
}
