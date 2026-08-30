package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.IcatianPriest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrderOfTheEbonHand.class, IcatianPriest.class})
class OrderOfTheEbonHandTest extends BaseCardTest {

    @Test
    @DisplayName("Black mana grants first strike until end of turn")
    void grantsFirstStrike() {
        Permanent order = addCreatureReady(player1, new OrderOfTheEbonHand());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, order, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, order, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Two black mana grants +1/+0 until end of turn")
    void grantsPowerBoost() {
        Permanent order = addCreatureReady(player1, new OrderOfTheEbonHand());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, order)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, order)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, order)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, order)).isEqualTo(1);
    }

    @Test
    @DisplayName("Protection from white prevents a white creature from blocking")
    void protectionFromWhitePreventsBlocking() {
        Permanent order = addCreatureReady(player1, new OrderOfTheEbonHand());
        order.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new IcatianPriest());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(order)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from white prevents a white ability from targeting it")
    void protectionFromWhitePreventsWhiteAbilityTargeting() {
        Permanent order = addCreatureReady(player1, new OrderOfTheEbonHand());
        addCreatureReady(player2, new IcatianPriest());
        harness.addMana(player2, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, order.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Protection from white prevents combat damage from white creatures")
    void protectionFromWhitePreventsCombatDamage() {
        addCreatureReady(player1, new IcatianPriest());
        addCreatureReady(player2, new OrderOfTheEbonHand());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Order of the Ebon Hand");
        harness.assertInGraveyard(player1, "Icatian Priest");
    }
}
