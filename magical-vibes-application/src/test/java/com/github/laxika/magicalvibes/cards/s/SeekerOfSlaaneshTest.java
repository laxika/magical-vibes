package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelMutation;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeekerOfSlaanesh.class, GrizzlyBears.class, DarksteelMutation.class})
class SeekerOfSlaaneshTest extends BaseCardTest {

    @Test
    void eachOpponentMustAttackWithAtLeastOneCreature() {
        harness.addToBattlefield(player1, new SeekerOfSlaanesh());
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must attack with at least one creature");
    }

    @Test
    void oneOpponentCreatureCanAttack() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SeekerOfSlaanesh());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(bears.isAttacking()).isTrue();
    }

    @Test
    void controllerIsNotRequiredToAttack() {
        harness.addToBattlefield(player1, new SeekerOfSlaanesh());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of());
    }

    @Test
    void opponentMayChooseOnlyOneOfSeveralCreatures() {
        harness.addToBattlefield(player1, new SeekerOfSlaanesh());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(1));

        assertThat(first.isAttacking()).isFalse();
        assertThat(second.isAttacking()).isTrue();
    }

    @Test
    void opponentWithoutCreaturesMayDeclineToAttack() {
        harness.addToBattlefield(player1, new SeekerOfSlaanesh());

        declareAttackers(player2, List.of());
    }

    @Test
    void opponentWithOnlyTappedCreaturesMayDeclineToAttack() {
        harness.addToBattlefield(player1, new SeekerOfSlaanesh());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setTapped(true);

        declareAttackers(player2, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    void opponentWithOnlySummoningSickCreaturesMayDeclineToAttack() {
        harness.addToBattlefield(player1, new SeekerOfSlaanesh());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(true);

        declareAttackers(player2, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    void seekerCanAttackTheTurnItEnters() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new SeekerOfSlaanesh());
        seeker.setSummoningSick(true);

        declareAttackers(player1, List.of(0));

        assertThat(seeker.isAttacking()).isTrue();
    }

    @Test
    void losingAllAbilitiesRemovesTheAttackRequirement() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new SeekerOfSlaanesh());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DarksteelMutation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, seeker.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }
}
