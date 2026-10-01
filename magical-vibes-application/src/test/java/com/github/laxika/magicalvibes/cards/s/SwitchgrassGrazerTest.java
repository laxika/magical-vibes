package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwitchgrassGrazer.class, GrizzlyBears.class})
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
        Permanent grazer = addCreatureReady(player1, new SwitchgrassGrazer());
        grazer.setSaddled(true);
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("An unsaddled attack does not trigger")
    void unsaddledAttackDoesNotTrigger() {
        addCreatureReady(player1, new SwitchgrassGrazer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(bls.canBlock(gd, target)).isTrue();
    }

    @Test
    @DisplayName("The attack trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent grazer = addCreatureReady(player1, new SwitchgrassGrazer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        grazer.setSaddled(true);
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isZero();
    }
}
