package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinMotivator.class, GreenwoodSentinel.class, Forest.class})
class GoblinMotivatorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability grants haste to target creature")
    void resolvingGrantsHaste() {
        addCreatureReady(player1, new GoblinMotivator());
        Permanent target = addCreatureReady(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentCreature() {
        addCreatureReady(player1, new GoblinMotivator());
        Permanent target = addCreatureReady(player2, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste is removed at end of turn")
    void hasteRemovedAtEndOfTurn() {
        addCreatureReady(player1, new GoblinMotivator());
        Permanent target = addCreatureReady(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new GoblinMotivator());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void tapsAsCostAndGrantsHasteOnlyOnResolution() {
        Permanent motivator = addCreatureReady(player1, new GoblinMotivator());
        Permanent target = addCreatureReady(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(motivator.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void summoningSickMotivatorCannotActivateEvenTargetingItself() {
        Permanent motivator = harness.addToBattlefieldAndReturn(player1, new GoblinMotivator());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, motivator.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(motivator.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetItself() {
        Permanent motivator = addCreatureReady(player1, new GoblinMotivator());

        harness.activateAbility(player1, 0, null, motivator.getId());
        harness.passBothPriorities();

        assertThat(motivator.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(motivator.isTapped()).isTrue();
    }

    @Test
    void grantedHasteAllowsNewMotivatorToPayTapCost() {
        addCreatureReady(player1, new GoblinMotivator());
        Permanent newMotivator = harness.addToBattlefieldAndReturn(player1, new GoblinMotivator());
        Permanent target = addCreatureReady(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, null, newMotivator.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(newMotivator.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent motivator = addCreatureReady(player1, new GoblinMotivator());
        Permanent target = addCreatureReady(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(motivator);
        gd.playerGraveyards.get(player1.getId()).add(motivator.getCard());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void grantedHasteAllowsNewCreatureToAttack() {
        addCreatureReady(player1, new GoblinMotivator());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(player1, List.of(1));

        assertThat(gd.declaredAttackerIdsThisCombat).contains(target.getId());
    }

    @Test
    void removedTargetDoesNotReceiveHaste() {
        Permanent motivator = addCreatureReady(player1, new GoblinMotivator());
        Permanent target = addCreatureReady(player1, new GreenwoodSentinel());
        Permanent other = addCreatureReady(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(other.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(motivator.isTapped()).isTrue();
    }
}
