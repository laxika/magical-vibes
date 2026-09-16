package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.d.DaruLancer;
import com.github.laxika.magicalvibes.cards.g.GluttonousZombie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WalkingDesecration.class, AvianChangeling.class, GluttonousZombie.class, DaruLancer.class})
class WalkingDesecrationTest extends BaseCardTest {

    @Test
    @DisplayName("Forces every creature of the chosen type on both sides to attack")
    void forcesChosenTypeToAttack() {
        Permanent walkingDesecration = addCreatureReady(player1, new WalkingDesecration());
        Permanent ownZombie = addCreatureReady(player1, new GluttonousZombie());
        Permanent opponentZombie = addCreatureReady(player2, new GluttonousZombie());
        Permanent human = addCreatureReady(player1, new DaruLancer());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ZOMBIE");

        assertThat(walkingDesecration.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(walkingDesecration.isMustAttackThisTurn()).isTrue();
        assertThat(ownZombie.isMustAttackThisTurn()).isTrue();
        assertThat(opponentZombie.isMustAttackThisTurn()).isTrue();
        assertThat(human.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Counts a Changeling as a creature of the chosen type")
    void changelingMatchesChosenType() {
        addCreatureReady(player1, new WalkingDesecration());
        Permanent changeling = addCreatureReady(player2, new AvianChangeling());
        Permanent human = addCreatureReady(player1, new DaruLancer());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(changeling.isMustAttackThisTurn()).isTrue();
        assertThat(human.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Requires a matching creature to be declared as an attacker when able")
    void requiresMatchingCreatureToAttackWhenAble() {
        addCreatureReady(player1, new WalkingDesecration());
        addCreatureReady(player1, new DaruLancer());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "HUMAN");

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("The must-attack requirement wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addCreatureReady(player1, new WalkingDesecration());
        Permanent opponentZombie = addCreatureReady(player2, new GluttonousZombie());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ZOMBIE");

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(opponentZombie.isMustAttackThisTurn()).isFalse();
    }
}
