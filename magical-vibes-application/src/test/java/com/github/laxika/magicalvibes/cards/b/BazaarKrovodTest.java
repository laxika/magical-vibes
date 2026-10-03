package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BazaarKrovod.class, DrudgeBeetle.class})
class BazaarKrovodTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives another attacking creature +0/+2 and untaps it")
    void boostsAndUntapsAnotherAttacker() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new BazaarKrovod());
        Permanent otherAttacker = addCreatureReady(player1, new DrudgeBeetle());

        declareAttackers(player1, List.of(0, 1));

        assertThat(otherAttacker.isTapped()).isTrue();

        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, otherAttacker)).isEqualTo(4);
        assertThat(otherAttacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The toughness boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new BazaarKrovod());
        Permanent otherAttacker = addCreatureReady(player1, new DrudgeBeetle());

        declareAttackers(player1, List.of(0, 1));

        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, otherAttacker)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, otherAttacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent krovod = addCreatureReady(player1, new BazaarKrovod());
        addCreatureReady(player1, new DrudgeBeetle());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, krovod.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that isn't attacking")
    void cannotTargetNonAttackingCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new BazaarKrovod());
        addCreatureReady(player1, new DrudgeBeetle());
        Permanent nonAttacker = addCreatureReady(player1, new DrudgeBeetle());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attackingAloneDoesNotBoostOrUntapItself() {
        Permanent krovod = addCreatureReady(player1, new BazaarKrovod());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(krovod.isTapped()).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, krovod)).isEqualTo(5);
    }

    @Test
    void alreadyUntappedAttackerStillGetsBoost() {
        addCreatureReady(player1, new BazaarKrovod());
        Permanent attacker = addCreatureReady(player1, new DrudgeBeetle());
        declareAttackers(List.of(0, 1));
        attacker.setTapped(false);

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    void targetThatStopsAttackingBeforeResolutionGetsNeitherEffect() {
        addCreatureReady(player1, new BazaarKrovod());
        Permanent attacker = addCreatureReady(player1, new DrudgeBeetle());
        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        attacker.setAttacking(false);

        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        assertThat(attacker.isTapped()).isTrue();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent krovod = addCreatureReady(player1, new BazaarKrovod());
        Permanent attacker = addCreatureReady(player1, new DrudgeBeetle());
        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(krovod);
        gd.playerGraveyards.get(player1.getId()).add(krovod.getCard());

        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        assertThat(attacker.isTapped()).isFalse();
    }
}
