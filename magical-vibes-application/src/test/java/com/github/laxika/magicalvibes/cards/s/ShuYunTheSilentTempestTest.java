package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShuYunTheSilentTempestTest extends BaseCardTest {

    private Permanent addShuYun() {
        harness.addToBattlefield(player1, new ShuYunTheSilentTempest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
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
        Permanent land = harness.addToBattlefieldAndReturn(player2, new com.github.laxika.magicalvibes.cards.f.Forest());
        castShock();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
