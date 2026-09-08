package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.ArmorThrull;
import com.github.laxika.magicalvibes.cards.b.BasalThrull;
import com.github.laxika.magicalvibes.cards.i.IcatianInfantry;
import com.github.laxika.magicalvibes.cards.i.IcatianPriest;
import com.github.laxika.magicalvibes.cards.t.ThrullRetainer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrderOfLeitbur.class, ArmorThrull.class, BasalThrull.class, IcatianInfantry.class,
        IcatianPriest.class})
class OrderOfLeitburTest extends BaseCardTest {

    @Test
    @DisplayName("White mana grants first strike until end of turn")
    void grantsFirstStrike() {
        Permanent order = addReadyOrder(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, order, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, order, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Two white mana grants +1/+0 until end of turn")
    void grantsPowerBoost() {
        Permanent order = addReadyOrder(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, order)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, order)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating either ability does not tap Order of Leitbur")
    void activatedAbilitiesDoNotTapSource() {
        Permanent order = addReadyOrder(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(order.isTapped()).isFalse();
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(order.isTapped()).isFalse();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Power boost lasts only until end of turn")
    void powerBoostExpiresAtEndOfTurn() {
        Permanent order = addReadyOrder(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, order)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, order)).isEqualTo(2);
    }

    @Test
    @DisplayName("Protection from black prevents a black creature from blocking")
    void protectionFromBlackPreventsBlocking() {
        Permanent order = addReadyOrder(player1);
        order.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BasalThrull());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, blocker), indexOf(player1, order)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A non-black creature can block Order of Leitbur")
    void nonBlackCreatureCanBlock() {
        Permanent order = addReadyOrder(player1);
        order.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new IcatianInfantry());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, blocker), indexOf(player1, order))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Protection from black prevents combat damage from a black creature")
    void protectionFromBlackPreventsCombatDamage() {
        Permanent attacker = addCreatureReady(player2, new BasalThrull());
        attacker.setAttacking(true);
        Permanent order = addReadyOrder(player1);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(indexOf(player1, order), indexOf(player2, attacker))));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Order of Leitbur");
        harness.assertInGraveyard(player2, "Basal Thrull");
    }

    @Test
    @DisplayName("Protection from black prevents a black ability from targeting Order of Leitbur")
    void protectionFromBlackPreventsBlackAbilityTargeting() {
        addCreatureReady(player1, new ArmorThrull());
        Permanent order = addReadyOrder(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, order.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Protection from black does not prevent a white ability from targeting Order of Leitbur")
    void whiteAbilityCanTargetOrderOfLeitbur() {
        addCreatureReady(player1, new IcatianPriest());
        Permanent order = addReadyOrder(player2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, order.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, order)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, order)).isEqualTo(2);
    }

    @Test
    @CardUsed(ThrullRetainer.class)
    @DisplayName("Protection from black prevents a black Aura from enchanting Order of Leitbur")
    void protectionFromBlackPreventsBlackAuraEnchanting() {
        Permanent order = addReadyOrder(player2);
        harness.setHand(player1, List.of(new ThrullRetainer()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, order.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    private Permanent addReadyOrder(Player player) {
        return addCreatureReady(player, new OrderOfLeitbur());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
