package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.cards.b.BorealGriffin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdarkarWindform.class, SnowCoveredPlains.class, BorealGriffin.class})
class AdarkarWindformTest extends BaseCardTest {

    @Test
    @DisplayName("Snow ability removes flying from target creature until end of turn")
    void removesFlyingUntilEndOfTurn() {
        addWindformReady(player1);
        Permanent target = addCreatureReady(player2, new BorealGriffin());
        payAbilityCost(player1);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Regular mana cannot pay the snow activation cost")
    void regularManaCannotPaySnowCost() {
        addWindformReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability rejects a non-creature target")
    void rejectsNonCreatureTarget() {
        addWindformReady(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SnowCoveredPlains());
        payAbilityCost(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Windform can target itself using colored snow mana")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent windform = harness.addToBattlefieldAndReturn(player1, new AdarkarWindform());
        windform.setSummoningSick(true);
        windform.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, windform.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, windform, Keyword.FLYING)).isFalse();
        assertThat(windform.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("One snow mana cannot pay both the generic and snow symbols")
    void requiresTwoManaIncludingOneSnowMana() {
        Permanent windform = addWindformReady(player1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, windform.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability resolves even after Windform leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent windform = addWindformReady(player1);
        Permanent target = addCreatureReady(player2, new BorealGriffin());
        payAbilityCost(player1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(windform);
        gd.playerGraveyards.get(player1.getId()).add(windform.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature without flying remains a legal target")
    void canTargetCreatureAfterItLosesFlying() {
        addWindformReady(player1);
        Permanent target = addCreatureReady(player2, new BorealGriffin());
        payAbilityCost(player1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();

        payAbilityCost(player1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addWindformReady(Player player) {
        return addCreatureReady(player, new AdarkarWindform());
    }

    private void payAbilityCost(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        gd.playerManaPools.get(player.getId()).addSnowMana(ManaColor.COLORLESS, 1);
    }
}
