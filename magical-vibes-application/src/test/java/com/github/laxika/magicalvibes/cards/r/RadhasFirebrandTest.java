package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({RadhasFirebrand.class, GrizzlyBears.class, HillGiant.class, Forest.class, Island.class})
class RadhasFirebrandTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger targets only a lower-power creature defending player controls")
    void attackTriggerRestrictsTargetsByDefendingPlayerAndPower() {
        Permanent firebrand = addCreatureReady(player1, new RadhasFirebrand());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lowerPowerCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equalPowerCreature = addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(lowerPowerCreature.getId())
                .doesNotContain(firebrand.getId(), ownCreature.getId(), equalPowerCreature.getId());

        harness.handlePermanentChosen(player1, lowerPowerCreature.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, lowerPowerCreature, firebrand,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, equalPowerCreature, firebrand,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Domain reduces the activated ability by one for each distinct basic land type")
    void domainReducesActivationCost() {
        Permanent firebrand = addCreatureReady(player1, new RadhasFirebrand());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(firebrand.getEffectivePower()).isEqualTo(5);
        assertThat(firebrand.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The activated ability can be used only once each turn and wears off at end of turn")
    void activationIsLimitedAndTemporary() {
        Permanent firebrand = addCreatureReady(player1, new RadhasFirebrand());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firebrand.getEffectivePower()).isEqualTo(5);
        assertThat(firebrand.getEffectiveToughness()).isEqualTo(3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(firebrand.getEffectivePower()).isEqualTo(3);
        assertThat(firebrand.getEffectiveToughness()).isEqualTo(1);
    }
}
