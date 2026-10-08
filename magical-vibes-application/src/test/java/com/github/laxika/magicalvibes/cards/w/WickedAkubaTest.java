package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AkkiAvalanchers;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.h.Hankyu;
import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({WickedAkuba.class, HumbleBudoka.class, AkkiAvalanchers.class, RendSpirit.class, Hankyu.class})
class WickedAkubaTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    private void addReadyAkuba() {
        addCreatureReady(player1, new WickedAkuba());
    }

    @Test
    @DisplayName("A player dealt combat damage by Wicked Akuba loses 1 life to the ability")
    void damagedPlayerLosesLife() {
        addReadyAkuba();

        declareAttackers(List.of(0));
        resolveCombat();
        // Mana empties between steps, so pay for the ability after combat damage is dealt.
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(STARTING_LIFE - 2 - 1);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly on the same damaged player")
    void activatesRepeatedly() {
        addReadyAkuba();

        declareAttackers(List.of(0));
        resolveCombat();
        // Mana empties between steps, so pay for the ability after combat damage is dealt.
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(STARTING_LIFE - 2 - 2);
    }

    @Test
    @DisplayName("Cannot target a player Wicked Akuba has not damaged this turn")
    void cannotTargetUndamagedPlayer() {
        addReadyAkuba();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the controller when only the opponent was damaged")
    void cannotTargetUndamagedController() {
        addReadyAkuba();

        declareAttackers(List.of(0));
        resolveCombat();
        // Mana empties between steps, so pay for the ability after combat damage is dealt.
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage dealt by another creature does not make its victim a legal target")
    void otherCreaturesDamageDoesNotCount() {
        addReadyAkuba();
        addCreatureReady(player1, new HumbleBudoka());

        declareAttackers(List.of(1));
        resolveCombat();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A blocked attack does not make the player a legal target")
    void blockedDamageDoesNotCount() {
        addReadyAkuba();
        addCreatureReady(player2, new AkkiAvalanchers());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(STARTING_LIFE);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage from one Wicked Akuba does not enable another copy's ability")
    void damageIsTrackedSeparatelyForEachCopy() {
        addReadyAkuba();
        addReadyAkuba();

        declareAttackers(List.of(0));
        resolveCombat();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, STARTING_LIFE - 3);
    }

    @Test
    @DisplayName("The activated ability still resolves after Wicked Akuba is destroyed")
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent akuba = addCreatureReady(player1, new WickedAkuba());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.setHand(player2, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.castInstant(player2, 0, akuba.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wicked Akuba");
        harness.assertLife(player2, STARTING_LIFE - 3);
    }

    @Test
    @DisplayName("Damage dealt during the previous turn does not enable the ability")
    void damageHistoryExpiresAtTurnBoundary() {
        addReadyAkuba();

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, STARTING_LIFE - 2);
    }

    @Test
    @DisplayName("Noncombat damage enables the ability even while Wicked Akuba is tapped")
    void noncombatDamageEnablesAbility() {
        Permanent akuba = addCreatureReady(player1, new WickedAkuba());
        Permanent hankyu = harness.addToBattlefieldAndReturn(player1, new Hankyu());
        hankyu.setAttachedTo(akuba.getId());
        hankyu.setCounterCount(CounterType.AIM, 1);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, STARTING_LIFE - 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, STARTING_LIFE - 2);
    }

    @Test
    @DisplayName("Dealing zero noncombat damage does not enable the ability")
    void zeroDamageDoesNotEnableAbility() {
        Permanent akuba = addCreatureReady(player1, new WickedAkuba());
        Permanent hankyu = harness.addToBattlefieldAndReturn(player1, new Hankyu());
        hankyu.setAttachedTo(akuba.getId());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, STARTING_LIFE);
    }
}
