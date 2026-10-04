package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BronzeBombshell;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.cards.r.RakdosIckspitter;
import com.github.laxika.magicalvibes.cards.s.ShieldingPlax;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
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

@CardUsed({EnemyOfTheGuildpact.class, BronzeBombshell.class, HillGiant.class, Mortify.class, RakdosIckspitter.class,
        ShieldingPlax.class, Shock.class, TurnToFrog.class, WoollyThoctar.class})
class EnemyOfTheGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from multicolored sources but not monocolored sources")
    void hasProtectionFromMulticoloredSources() {
        Permanent enemy = addCreatureReady(player1, new EnemyOfTheGuildpact());
        Permanent multicoloredSource = addCreatureReady(player2, new WoollyThoctar());
        Permanent monocoloredSource = addCreatureReady(player2, new HillGiant());

        assertThat(gqs.hasProtectionFromSource(gd, enemy, multicoloredSource)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, enemy, monocoloredSource)).isFalse();
    }

    @Test
    @DisplayName("A multicolored creature cannot block Enemy of the Guildpact")
    void multicoloredCreatureCannotBlock() {
        Permanent enemy = addCreatureReady(player1, new EnemyOfTheGuildpact());
        enemy.setAttacking(true);
        addCreatureReady(player2, new WoollyThoctar());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Combat damage from a multicolored creature is prevented")
    void multicoloredCombatDamageIsPrevented() {
        Permanent attacker = addCreatureReady(player1, new WoollyThoctar());
        attacker.setAttacking(true);
        Permanent enemy = addCreatureReady(player2, new EnemyOfTheGuildpact());
        enemy.setBlocking(true);
        enemy.addBlockingTarget(0);

        resolveCombat();

        assertThat(enemy.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A multicolored spell cannot target Enemy of the Guildpact")
    void multicoloredSpellCannotTarget() {
        Permanent enemy = addCreatureReady(player2, new EnemyOfTheGuildpact());
        harness.setHand(player1, List.of(new Mortify()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enemy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A monocolored spell can target Enemy of the Guildpact")
    void monocoloredSpellCanTarget() {
        Permanent enemy = addCreatureReady(player2, new EnemyOfTheGuildpact());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, enemy.getId());
        harness.passBothPriorities();

        assertThat(enemy.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A multicolored activated ability cannot target Enemy of the Guildpact")
    void multicoloredActivatedAbilityCannotTarget() {
        Permanent enemy = addCreatureReady(player2, new EnemyOfTheGuildpact());
        Permanent source = addCreatureReady(player1, new RakdosIckspitter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enemy.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A hybrid multicolored Aura cannot enchant Enemy even when paid with one color")
    void multicoloredAuraCannotTarget() {
        Permanent enemy = addCreatureReady(player1, new EnemyOfTheGuildpact());
        harness.setHand(player1, List.of(new ShieldingPlax()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enemy.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An attached multicolored Aura is put into its owner's graveyard")
    void multicoloredAuraCannotRemainAttached() {
        Permanent enemy = addCreatureReady(player1, new EnemyOfTheGuildpact());
        ShieldingPlax plax = new ShieldingPlax();
        plax.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, plax);
        aura.setAttachedTo(enemy.getId());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(enemy).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(plax);
    }

    @Test
    @DisplayName("Monocolored creatures can block and deal lethal damage to Enemy")
    void monocoloredCreatureCanBlockAndDealDamage() {
        Permanent enemy = addCreatureReady(player1, new EnemyOfTheGuildpact());
        enemy.setAttacking(true);
        addCreatureReady(player2, new HillGiant());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enemy);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enemy.getCard());
    }

    @Test
    @DisplayName("Colorless creatures can block and deal lethal damage to Enemy")
    void colorlessCreatureCanBlockAndDealDamage() {
        Permanent enemy = addCreatureReady(player1, new EnemyOfTheGuildpact());
        enemy.setAttacking(true);
        BronzeBombshell bombshell = new BronzeBombshell();
        bombshell.setOwnerId(player2.getId());
        addCreatureReady(player2, bombshell);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enemy);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enemy.getCard());
    }

    @Test
    @DisplayName("Enemy can be targeted by a multicolored spell after losing all abilities")
    void losingAbilitiesAllowsMulticoloredTargeting() {
        Permanent enemy = addCreatureReady(player2, new EnemyOfTheGuildpact());
        turnToFrog(enemy);
        harness.setHand(player1, List.of(new Mortify()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, enemy.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enemy);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(enemy.getCard());
    }

    @Test
    @DisplayName("Enemy takes multicolored combat damage after losing all abilities")
    void losingAbilitiesAllowsMulticoloredCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new WoollyThoctar());
        Permanent enemy = addCreatureReady(player2, new EnemyOfTheGuildpact());
        turnToFrog(enemy);
        attacker.setAttacking(true);
        enemy.setBlocking(true);
        enemy.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enemy);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(enemy.getCard());
    }

    @Test
    @DisplayName("Multicolored creatures can block Enemy after it loses all abilities")
    void losingAbilitiesAllowsMulticoloredBlockers() {
        Permanent enemy = addCreatureReady(player1, new EnemyOfTheGuildpact());
        addCreatureReady(player2, new WoollyThoctar());
        turnToFrog(enemy);
        enemy.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enemy);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enemy.getCard());
    }

    private void turnToFrog(Permanent target) {
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
