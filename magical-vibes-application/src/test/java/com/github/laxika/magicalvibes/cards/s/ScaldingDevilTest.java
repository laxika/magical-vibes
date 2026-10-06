package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RagingPoltergeist;
import com.github.laxika.magicalvibes.cards.t.TibaltTheFiendBlooded;
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

@CardUsed({ScaldingDevil.class, RagingPoltergeist.class, TibaltTheFiendBlooded.class})
class ScaldingDevilTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 1 damage to target player")
    void dealsOneDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        addReadyDevil(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Ability does not tap the creature, so it can be activated repeatedly")
    void abilityDoesNotTapAndCanRepeat() {
        harness.setLife(player2, 20);
        Permanent devil = addReadyDevil(player1);
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(devil.isTapped()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A planeswalker is a legal target; damage removes a loyalty counter")
    void damagesPlaneswalker() {
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new TibaltTheFiendBlooded());
        addReadyDevil(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature is not a legal target")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RagingPoltergeist());
        addReadyDevil(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    private Permanent addReadyDevil(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ScaldingDevil());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent devil = harness.addToBattlefieldAndReturn(player1, new ScaldingDevil());
        devil.setSummoningSick(true);
        devil.setTapped(true);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(devil.isTapped()).isTrue();
    }

    @Test
    void canTargetController() {
        addReadyDevil(player1);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent devil = addReadyDevil(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(devil);
        gd.playerGraveyards.get(player1.getId()).add(devil.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void cannotActivateWithoutRedMana() {
        addReadyDevil(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(RuntimeException.class);
        assertThat(gd.stack).isEmpty();
    }
}
