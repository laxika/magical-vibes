package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scaldkin.class, WetlandSambar.class, SarkhanTheDragonspeaker.class})
class ScaldkinTest extends BaseCardTest {

    @Test
    @DisplayName("Scaldkin deals 2 damage to target player")
    void dealsDamageToPlayer() {
        addReadyScaldkin(player1);
        harness.setLife(player2, 20);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Scaldkin is sacrificed as part of the activation cost")
    void sacrificedAsCost() {
        addReadyScaldkin(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Scaldkin");
        harness.assertInGraveyard(player1, "Scaldkin");
    }

    @Test
    @DisplayName("Scaldkin deals 2 damage to target creature")
    void dealsDamageToCreature() {
        addReadyScaldkin(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Wetland Sambar");
    }

    @Test
    @DisplayName("Cannot activate Scaldkin without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyScaldkin(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Scaldkin deals exactly 2 damage to a planeswalker")
    void dealsDamageToPlaneswalker() {
        addReadyScaldkin(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SarkhanTheDragonspeaker());
        target.setCounterCount(CounterType.LOYALTY, 4);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Sarkhan, the Dragonspeaker");
    }

    @Test
    @DisplayName("Scaldkin can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent scaldkin = harness.addToBattlefieldAndReturn(player1, new Scaldkin());
        scaldkin.setSummoningSick(true);
        scaldkin.setTapped(true);
        harness.setLife(player1, 20);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Scaldkin");
    }

    @Test
    @DisplayName("A sacrificed Scaldkin can be its own target but deals no damage")
    void canTargetItself() {
        Permanent scaldkin = addReadyScaldkin(player1);
        harness.setLife(player1, 20);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, scaldkin.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scaldkin");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyScaldkin(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new Scaldkin());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.RED, 1);
    }
}
