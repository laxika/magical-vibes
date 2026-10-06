package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShuYunTheSilentTempest.class, Shock.class, GrizzlyBears.class, Forest.class})
class ShuYunTheSilentTempestTest extends BaseCardTest {

    private Permanent addShuYun() {
        Permanent shuYun = harness.addToBattlefieldAndReturn(player1, new ShuYunTheSilentTempest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return shuYun;
    }

    private void castShock() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Casting a noncreature spell gives Shu Yun +1/+1")
    void noncreatureSpellGivesProwess() {
        Permanent shuYun = addShuYun();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castShock();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, shuYun)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shuYun)).isEqualTo(3);
    }

    @Test
    @DisplayName("Paying two hybrid mana gives target creature double strike until end of turn")
    void payingHybridManaGrantsDoubleStrike() {
        addShuYun();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castShock();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Declining the payment does not grant double strike")
    void decliningPaymentDoesNothing() {
        addShuYun();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castShock();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The trigger only allows creature targets")
    void targetMustBeCreature() {
        addShuYun();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        castShock();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mixedHybridPaymentCanTargetShuYunAndEffectsExpire() {
        Permanent shuYun = addShuYun();
        castShock();
        harness.handlePermanentChosen(player1, shuYun.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(shuYun.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, shuYun)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shuYun)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(shuYun.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, shuYun)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shuYun)).isEqualTo(2);
    }

    @Test
    void eachNoncreatureSpellTriggersAndRedManaPaysHybridCost() {
        Permanent shuYun = addShuYun();
        for (int i = 0; i < 2; i++) {
            castShock();
            harness.handlePermanentChosen(player1, shuYun.getId());
            harness.passBothPriorities();
            harness.addMana(player1, ManaColor.RED, 2);
            harness.handleMayAbilityChosen(player1, true);
            while (!gd.stack.isEmpty()) {
                harness.passBothPriorities();
            }
        }

        assertThat(shuYun.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, shuYun)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, shuYun)).isEqualTo(4);
        harness.assertLife(player2, 16);
    }

    @Test
    void blueManaCannotPayHybridCost() {
        Permanent shuYun = addShuYun();
        castShock();
        harness.handlePermanentChosen(player1, shuYun.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(shuYun.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, shuYun)).isEqualTo(4);
    }

    @Test
    void creatureSpellDoesNotTriggerEitherAbility() {
        Permanent shuYun = addShuYun();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, shuYun)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shuYun)).isEqualTo(2);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerEitherAbility() {
        Permanent shuYun = addShuYun();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, shuYun)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shuYun)).isEqualTo(2);
    }
}
