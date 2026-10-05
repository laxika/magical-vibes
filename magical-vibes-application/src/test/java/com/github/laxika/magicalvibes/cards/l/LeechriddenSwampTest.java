package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.SickleRipper;

import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeechriddenSwamp.class, SickleRipper.class, SafeholdElite.class})
class LeechriddenSwampTest extends BaseCardTest {

    @Test
    @DisplayName("Drain ability makes each opponent lose 1 life when controlling two or more black permanents")
    void drainWithTwoBlackPermanents() {
        Permanent swamp = addSwamp(player1);
        addBlackPermanents(player1, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);

        int swampIdx = gd.playerBattlefields.get(player1.getId()).indexOf(swamp);
        harness.activateAbility(player1, swampIdx, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Drain ability cannot be activated with fewer than two black permanents")
    void drainRejectedWithTooFewBlackPermanents() {
        Permanent swamp = addSwamp(player1);
        // One black permanent — the colorless swamp does not make up the second.
        addBlackPermanents(player1, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int swampIdx = gd.playerBattlefields.get(player1.getId()).indexOf(swamp);
        assertThatThrownBy(() -> harness.activateAbility(player1, swampIdx, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-black permanents do not count toward the activation restriction")
    void nonBlackPermanentsDoNotCount() {
        Permanent swamp = addSwamp(player1);
        harness.addToBattlefield(player1, new SafeholdElite());
        harness.addToBattlefield(player1, new SafeholdElite());
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Neither the colorless swamp nor the green-white creatures count.
        int swampIdx = gd.playerBattlefields.get(player1.getId()).indexOf(swamp);
        assertThatThrownBy(() -> harness.activateAbility(player1, swampIdx, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap ability adds black mana")
    void manaAbilityAddsBlack() {
        Permanent swamp = addSwamp(player1);

        int swampIdx = gd.playerBattlefields.get(player1.getId()).indexOf(swamp);
        harness.activateAbility(player1, swampIdx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void entersTapped() {
        Permanent swamp = harness.enterBattlefieldAndReturn(player1, new LeechriddenSwamp());
        assertThat(swamp.isTapped()).isTrue();
    }

    @Test
    void drainPaysCostsWithoutGainingLife() {
        Permanent swamp = addSwamp(player1);
        addBlackPermanents(player1, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(swamp.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void restrictionIsNotCheckedAgainOnResolution() {
        Permanent swamp = addSwamp(player1);
        addBlackPermanents(player1, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p != swamp);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void opponentsBlackPermanentsDoNotCount() {
        addSwamp(player1);
        addBlackPermanents(player1, 1);
        addBlackPermanents(player2, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drainCannotActivateWithoutBlackMana() {
        addSwamp(player1);
        addBlackPermanents(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSwamp(Player player) {
        Permanent swamp = harness.addToBattlefieldAndReturn(player, new LeechriddenSwamp());
        swamp.setSummoningSick(false);
        swamp.untap();
        return swamp;
    }

    private void addBlackPermanents(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new SickleRipper());
        }
    }
}

