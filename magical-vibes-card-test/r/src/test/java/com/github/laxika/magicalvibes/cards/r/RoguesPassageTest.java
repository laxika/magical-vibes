package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AxebaneStag;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoguesPassage.class, AxebaneStag.class})
class RoguesPassageTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: adds {C} without using the stack")
    void tapsForColorless() {
        Permanent passage = addCreatureReady(player1, new RoguesPassage());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(passage.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{4}, {T}: target creature can't be blocked this turn")
    void makesTargetCreatureUnblockable() {
        Permanent passage = addCreatureReady(player1, new RoguesPassage());
        Permanent bears = addCreatureReady(player1, new AxebaneStag());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(passage.isTapped()).isTrue();
        assertThat(bears.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off at cleanup")
    void unblockableWearsOffAtCleanup() {
        addCreatureReady(player1, new RoguesPassage());
        Permanent bears = addCreatureReady(player1, new AxebaneStag());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isFalse();
    }

    @Test
    void canTargetOpponentsCreature() {
        addCreatureReady(player1, new RoguesPassage());
        Permanent target = addCreatureReady(player2, new AxebaneStag());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void targetCannotBeBlockedButOtherCreaturesCan() {
        addCreatureReady(player1, new RoguesPassage());
        Permanent target = addCreatureReady(player1, new AxebaneStag());
        Permanent other = addCreatureReady(player1, new AxebaneStag());
        addCreatureReady(player2, new AxebaneStag());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(other.isCantBeBlocked()).isFalse();
        declareAttackersAndPrepareBlockers(List.of(1, 2));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 2)));
    }

    @Test
    void cannotActivateWithoutFourMana() {
        Permanent passage = addCreatureReady(player1, new RoguesPassage());
        Permanent target = addCreatureReady(player1, new AxebaneStag());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(passage.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetANoncreature() {
        Permanent passage = addCreatureReady(player1, new RoguesPassage());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, passage.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landCanTapForManaOnTurnItEnters() {
        Permanent passage = harness.addToBattlefieldAndReturn(player1, new RoguesPassage());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(passage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
