package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarsToll.class, Forest.class, GrizzlyBears.class})
class WarsTollTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent tapping a land for mana eventually taps all their lands")
    void opponentLandTapTapsAllTheirLands() {
        harness.addToBattlefield(player1, new WarsToll());
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player2);

        harness.tapPermanent(player2, 0);

        assertThat(tappedLand.isTapped()).isTrue();
        assertThat(otherLand.isTapped()).isFalse();

        resolveLandTapTriggers();

        assertThat(tappedLand.isTapped()).isTrue();
        assertThat(otherLand.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Tapping your own land does not trigger War's Toll")
    void ownLandTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new WarsToll());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.tapPermanent(player1, 1);
        resolveLandTapTriggers();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
    }

    @Test
    @DisplayName("When one opponent creature attacks, all of that opponent's able creatures must attack")
    void opponentCreaturesMustAttackTogether() {
        harness.addToBattlefield(player1, new WarsToll());
        Permanent first = addReadyCreature(player2);
        Permanent second = addReadyCreature(player2);
        beginOpponentDeclareAttackers();

        List<Integer> attackable = harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId());
        assertThat(harness.getCombatAttackService().getMustAttackAlongsideIndices(
                gd, player2.getId(), attackable, List.of(0))).containsExactly(1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0),
                Map.of(0, player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(second.getCard().getName());
        assertThat(first.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("War's Toll does not force its controller's creatures to attack together")
    void controllerCreaturesAreNotForcedTogether() {
        harness.addToBattlefield(player1, new WarsToll());
        addReadyCreature(player1);
        addReadyCreature(player1);

        assertThat(harness.getCombatAttackService().getMustAttackAlongsideIndices(
                gd, player1.getId(), List.of(1, 2), List.of(1))).isEmpty();
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private void beginOpponentDeclareAttackers() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void resolveLandTapTriggers() {
        for (int i = 0; i < 4 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
