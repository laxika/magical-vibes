package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.w.Watchwolf;
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

@CardUsed({Ursapine.class, Watchwolf.class})
class UrsapineTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Ursapine gives a target creature +1/+1 until end of turn")
    void activatesToBoostTargetCreature() {
        addUrsapineReady(player1);
        Permanent wolf = addCreatureReady(player1, new Watchwolf());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, wolf.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Ursapine's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addUrsapineReady(player1);
        Permanent wolf = addCreatureReady(player1, new Watchwolf());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, wolf.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ursapine can target an opponent's creature")
    void activatesOnOpponentsCreature() {
        addUrsapineReady(player1);
        Permanent wolf = addCreatureReady(player2, new Watchwolf());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, wolf.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Ursapine can be activated repeatedly without tapping")
    void canBeActivatedRepeatedlyWithoutTapping() {
        Permanent ursapine = addUrsapineReady(player1);
        Permanent wolf = addCreatureReady(player1, new Watchwolf());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, wolf.getId());
        assertThat(ursapine.isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, wolf.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(5);
    }

    @Test
    @DisplayName("Ursapine cannot target a player")
    void cannotTargetPlayer() {
        addUrsapineReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addUrsapineReady(Player player) {
        return addCreatureReady(player, new Ursapine());
    }

    @Test
    @DisplayName("A tapped, summoning-sick Ursapine can boost itself")
    void canBoostItselfWhileTappedAndSummoningSick() {
        Permanent ursapine = harness.addToBattlefieldAndReturn(player1, new Ursapine());
        ursapine.setSummoningSick(true);
        ursapine.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, ursapine.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ursapine)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ursapine)).isEqualTo(4);
        assertThat(ursapine.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ursapine's ability requires green mana")
    void cannotPayWithOtherColoredMana() {
        Permanent ursapine = addUrsapineReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ursapine.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, ursapine)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ursapine)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ursapine's ability resolves after its source leaves the battlefield")
    void boostResolvesAfterSourceLeavesBattlefield() {
        Permanent ursapine = addUrsapineReady(player1);
        Permanent wolf = addCreatureReady(player1, new Watchwolf());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, wolf.getId());
        gd.playerBattlefields.get(player1.getId()).remove(ursapine);
        gd.playerGraveyards.get(player1.getId()).add(ursapine.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(4);
    }
}
