package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({SquealingDevil.class, GrizzlyBears.class, Mountain.class})
class SquealingDevilTest extends BaseCardTest {

    @Test
    @DisplayName("Pays X to give a target creature +X/+0 until end of turn")
    void paysXToBoostTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SquealingDevil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player1, "Squealing Devil");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Choosing X=0 does not boost the target")
    void choosingZeroDoesNotBoostTarget() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SquealingDevil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Squealing Devil");
    }

    @Test
    @DisplayName("Is sacrificed when black mana was not spent to cast it")
    void isSacrificedWithoutBlackMana() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SquealingDevil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Squealing Devil");
        harness.assertInGraveyard(player1, "Squealing Devil");
    }

    @Test
    @DisplayName("The enter ability can target only a creature")
    void enterAbilityTargetsOnlyCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new SquealingDevil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(nonCreature.getId());
    }
}
