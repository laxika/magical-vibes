package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChartoothCougar;
import com.github.laxika.magicalvibes.cards.e.ExtraArms;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.l.LingeringDeath;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({SilverKnight.class, GoblinBrigand.class, ChartoothCougar.class, SparkSpray.class,
        ExtraArms.class, LingeringDeath.class})
class SilverKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from red")
    void hasProtectionFromRed() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new SilverKnight());

        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Red creature cannot block Silver Knight")
    void redCreatureCannotBlock() {
        Permanent knight = addCreatureReady(player1, new SilverKnight());
        knight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GoblinBrigand());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Red combat damage to Silver Knight is prevented")
    void redCombatDamageIsPrevented() {
        Permanent attacker = addCreatureReady(player1, new ChartoothCougar());
        attacker.setAttacking(true);
        Permanent knight = addCreatureReady(player2, new SilverKnight());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(knight.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Silver Knight");
    }

    @Test
    @DisplayName("Cannot be targeted by a red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent knight = addCreatureReady(player2, new SilverKnight());
        addCreatureReady(player2, new GoblinBrigand());

        harness.setHand(player1, List.of(new SparkSpray()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Cannot be enchanted by a red Aura")
    void cannotBeEnchantedByRedAura() {
        Permanent knight = addCreatureReady(player2, new SilverKnight());
        addCreatureReady(player2, new GoblinBrigand());

        harness.setHand(player1, List.of(new ExtraArms()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Can be enchanted by a non-red Aura")
    void canBeEnchantedByNonRedAura() {
        Permanent knight = addCreatureReady(player2, new SilverKnight());

        harness.setHand(player1, List.of(new LingeringDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, knight.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lingering Death");
    }
}
