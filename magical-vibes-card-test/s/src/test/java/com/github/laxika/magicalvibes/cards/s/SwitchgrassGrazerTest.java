package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({com.github.laxika.magicalvibes.cards.c.Clone.class, SwitchgrassGrazer.class, GrizzlyBears.class})
class SwitchgrassGrazerTest extends BaseCardTest {

    @Test
    @DisplayName("A saddled attack deals damage and permanently restricts a damaged creature")
    void saddledAttackAppliesPerpetualRiders() {
        Permanent grazer = addCreatureReady(player1, new SwitchgrassGrazer());
        grazer.setSaddled(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(bls.canBlock(gd, target)).isFalse();

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(bls.canBlock(gd, target)).isFalse();
    }

    @Test
    @DisplayName("The saddled attack can target a player")
    void saddledAttackCanTargetPlayer() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.EnumSet.of(TurnStep.DECLARE_BLOCKERS));
        Permanent grazer = addCreatureReady(player1, new SwitchgrassGrazer());
        grazer.setSaddled(true);
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("An unsaddled attack still deals 1 damage but gives no perpetual rider")
    void unsaddledAttackDealsDamageWithoutRiders() {
        addCreatureReady(player1, new SwitchgrassGrazer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(bls.canBlock(gd, target)).isTrue();
    }

    @Test
    @DisplayName("An unsaddled attack can deal its 1 damage to a player")
    void unsaddledAttackCanDamagePlayer() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.EnumSet.of(TurnStep.DECLARE_BLOCKERS));
        addCreatureReady(player1, new SwitchgrassGrazer());
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("A copy of the damaged creature copies neither perpetual rider")
    void copyOfDamagedCreatureDoesNotCopyThePerpetualRiders() {
        Permanent grazer = addCreatureReady(player1, new SwitchgrassGrazer());
        grazer.setSaddled(true);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.c.Clone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        Permanent copy = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> !permanent.getId().equals(target.getId()))
                .findFirst().orElseThrow();
        assertThat(bls.canBlock(gd, copy)).isTrue();
        assertThat(bls.canBlock(gd, target)).isFalse();
    }
}
