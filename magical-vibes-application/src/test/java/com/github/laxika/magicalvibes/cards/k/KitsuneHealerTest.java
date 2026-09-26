package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.y.YamabushisFlame;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KitsuneHealer.class, KokushoTheEveningStar.class, KitsuneBlademaster.class,
        YamabushisFlame.class})
class KitsuneHealerTest extends BaseCardTest {

    private Permanent addHealerReady() {
        return addCreatureReady(player1, new KitsuneHealer());
    }

    @Test
    @DisplayName("Prevents the next 1 damage to any target")
    void preventsNextDamageToAnyTarget() {
        addHealerReady();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents only the next 1 damage to a target player")
    void preventsOnlyNextDamageToPlayer() {
        addHealerReady();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        castYamabushisFlameAt(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDamagePreventionShields).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("Prevents only the next 1 damage to a target creature")
    void preventsOnlyNextDamageToCreature() {
        addHealerReady();
        Permanent target = addCreatureReady(player2, new KokushoTheEveningStar());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        castYamabushisFlameAt(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Prevents all damage to a target legendary creature")
    void preventsAllDamageToLegendaryCreature() {
        addHealerReady();
        harness.addToBattlefield(player2, new KokushoTheEveningStar());

        UUID targetId = harness.getPermanentId(player2, "Kokusho, the Evening Star");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.creaturesWithAllDamagePrevented).contains(targetId);
    }

    @Test
    @DisplayName("Prevents noncombat damage to a target legendary creature")
    void preventsNoncombatDamageToLegendaryCreature() {
        addHealerReady();
        Permanent target = addCreatureReady(player2, new KokushoTheEveningStar());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        castYamabushisFlameAt(target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Rejects a nonlegendary creature for the second ability")
    void rejectsNonlegendaryCreature() {
        addHealerReady();
        Permanent target = addCreatureReady(player2, new KitsuneBlademaster());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a player for the second ability")
    void rejectsPlayer() {
        addHealerReady();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castYamabushisFlameAt(UUID targetId) {
        harness.setHand(player1, List.of(new YamabushisFlame()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
