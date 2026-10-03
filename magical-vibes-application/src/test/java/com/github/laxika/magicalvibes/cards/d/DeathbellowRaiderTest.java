package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.n.NessianAsp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({DeathbellowRaider.class, NessianAsp.class})
class DeathbellowRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Deathbellow Raider must attack each combat if able")
    void mustAttackWhenAble() {
        addReadyRaider(player1);

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Activating Deathbellow Raider's regeneration grants a shield")
    void regeneratesItself() {
        addReadyRaider(player1);
        addRegenerationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Deathbellow Raider").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deathbellow Raider's regeneration shield prevents lethal damage")
    void regenerationShieldPreventsDestruction() {
        Permanent raider = addReadyRaider(player1);
        addRegenerationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        raider.setBlocking(true);
        raider.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new NessianAsp());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Deathbellow Raider");
        assertThat(findPermanent(player1, "Deathbellow Raider").getRegenerationShield()).isZero();
    }

    private Permanent addReadyRaider(Player player) {
        return addCreatureReady(player, new DeathbellowRaider());
    }

    @Test
    void tappedRaiderDoesNotHaveToAttack() {
        addReadyRaider(player1).tap();

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    void summoningSickRaiderDoesNotHaveToAttack() {
        addReadyRaider(player1).setSummoningSick(true);

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    void regenerationCanBeActivatedWhileTappedAndSummoningSick() {
        Permanent raider = addReadyRaider(player1);
        raider.setSummoningSick(true);
        raider.tap();
        addRegenerationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(raider.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void unusedRegenerationShieldDoesNotTapOrRemoveDamage() {
        Permanent raider = addReadyRaider(player1);
        raider.setMarkedDamage(1);
        addRegenerationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(raider.isTapped()).isFalse();
        assertThat(raider.getMarkedDamage()).isEqualTo(1);
        assertThat(raider.getRegenerationShield()).isEqualTo(1);
    }

    private void addRegenerationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
