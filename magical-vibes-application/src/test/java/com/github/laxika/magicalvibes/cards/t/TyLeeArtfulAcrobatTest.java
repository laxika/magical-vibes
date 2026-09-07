package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({TyLeeArtfulAcrobat.class, GrizzlyBears.class, Forest.class, Shock.class})
class TyLeeArtfulAcrobatTest extends BaseCardTest {

    @Test
    @DisplayName("Prowess gives Ty Lee +1/+1 until end of turn")
    void prowessPumpsUntilEndOfTurn() {
        Permanent tyLee = addCreatureReady(player1, new TyLeeArtfulAcrobat());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tyLee)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tyLee)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tyLee)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tyLee)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger prowess")
    void creatureSpellDoesNotTriggerProwess() {
        Permanent tyLee = addCreatureReady(player1, new TyLeeArtfulAcrobat());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, tyLee)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tyLee)).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying {1} makes the target creature unable to block this turn")
    void payingMakesTargetUnableToBlock() {
        addCreatureReady(player1, new TyLeeArtfulAcrobat());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttack();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Declining to pay leaves the target creature able to block")
    void decliningLeavesTargetAbleToBlock() {
        addCreatureReady(player1, new TyLeeArtfulAcrobat());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttack();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger targets creatures only")
    void attackTriggerTargetsCreaturesOnly() {
        addCreatureReady(player1, new TyLeeArtfulAcrobat());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttack();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId())
                .doesNotContain(forest.getId());
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        declareAttackers(player1, List.of(0));
    }
}
