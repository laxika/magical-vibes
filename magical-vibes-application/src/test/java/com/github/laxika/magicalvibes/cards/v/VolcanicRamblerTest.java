package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ChandraFireOfKaladesh;
import com.github.laxika.magicalvibes.cards.c.ChandraRoaringFlame;
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

@CardUsed({VolcanicRambler.class, ChandraFireOfKaladesh.class, ChandraRoaringFlame.class})
class VolcanicRamblerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 1 damage to target player")
    void dealsOneDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        addReadyRambler(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Ability does not tap the creature, so it can be activated repeatedly")
    void abilityDoesNotTapAndCanRepeat() {
        harness.setLife(player2, 20);
        Permanent rambler = addReadyRambler(player1);
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(rambler.isTapped()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A planeswalker is a legal target; damage removes a loyalty counter")
    void damagesPlaneswalker() {
        Permanent planeswalker = addPlaneswalker(player2, 4);
        addReadyRambler(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature is not a legal target")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new VolcanicRambler());
        addReadyRambler(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Ability can target its controller")
    void canTargetController() {
        harness.setLife(player1, 20);
        addReadyRambler(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        harness.setLife(player2, 20);
        Permanent rambler = harness.addToBattlefieldAndReturn(player1, new VolcanicRambler());
        rambler.setSummoningSick(true);
        rambler.tap();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(rambler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability still resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        harness.setLife(player2, 20);
        Permanent rambler = addReadyRambler(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(rambler);
        gd.playerGraveyards.get(player1.getId()).add(rambler.getCard());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Ability requires red mana, not just three generic mana")
    void cannotActivateWithoutRedMana() {
        addReadyRambler(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(RuntimeException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability can damage a planeswalker controlled by its controller")
    void canTargetOwnPlaneswalker() {
        addReadyRambler(player1);
        Permanent planeswalker = addPlaneswalker(player1, 4);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    private Permanent addReadyRambler(Player player) {
        return addCreatureReady(player, new VolcanicRambler());
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player,
                new ChandraFireOfKaladesh().getBackFaceCard());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }
}
