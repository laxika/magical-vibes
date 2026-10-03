package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.k.KnightOfTheKeep;
import com.github.laxika.magicalvibes.cards.r.RovingKeep;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BelleOfTheBrawl.class, KnightOfTheKeep.class, RovingKeep.class})
class BelleOfTheBrawlTest extends BaseCardTest {

    @Test
    void otherKnightsYouControlGetPlusOnePowerWhenBelleAttacks() {
        addCreatureReady(player1, new BelleOfTheBrawl());
        Permanent otherKnight = addCreatureReady(player1, new KnightOfTheKeep());
        Permanent nonKnight = addCreatureReady(player1, new RovingKeep());
        Permanent opposingKnight = addCreatureReady(player2, new KnightOfTheKeep());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(otherKnight.getEffectivePower()).isEqualTo(4);
        assertThat(otherKnight.getEffectiveToughness()).isEqualTo(2);
        assertThat(nonKnight.getEffectivePower()).isEqualTo(5);
        assertThat(opposingKnight.getEffectivePower()).isEqualTo(3);
    }

    @Test
    void BelleDoesNotBoostItself() {
        Permanent belle = addCreatureReady(player1, new BelleOfTheBrawl());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(belle.getEffectivePower()).isEqualTo(3);
        assertThat(belle.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new BelleOfTheBrawl());
        Permanent otherKnight = addCreatureReady(player1, new KnightOfTheKeep());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(otherKnight.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(otherKnight.getEffectivePower()).isEqualTo(3);
        assertThat(otherKnight.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void twoAttackingBellesBoostEachOtherAndStackTheirBoostsOnOtherKnights() {
        Permanent first = addCreatureReady(player1, new BelleOfTheBrawl());
        Permanent second = addCreatureReady(player1, new BelleOfTheBrawl());
        Permanent knight = addCreatureReady(player1, new KnightOfTheKeep());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(knight.getEffectivePower()).isEqualTo(5);
        assertThat(knight.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void boostAppliesToKnightsPresentAtResolutionOnly() {
        addCreatureReady(player1, new BelleOfTheBrawl());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        Permanent beforeResolution = addCreatureReady(player1, new KnightOfTheKeep());
        resolveAllTriggers();
        Permanent afterResolution = addCreatureReady(player1, new KnightOfTheKeep());

        assertThat(beforeResolution.getEffectivePower()).isEqualTo(4);
        assertThat(afterResolution.getEffectivePower()).isEqualTo(3);
    }

    @Test
    void attackTriggerStillResolvesAfterBelleLeavesTheBattlefield() {
        Permanent belle = addCreatureReady(player1, new BelleOfTheBrawl());
        Permanent knight = addCreatureReady(player1, new KnightOfTheKeep());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(belle);
        gd.playerGraveyards.get(player1.getId()).add(belle.getCard());
        resolveAllTriggers();

        assertThat(knight.getEffectivePower()).isEqualTo(4);
        assertThat(knight.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new BelleOfTheBrawl());
        addCreatureReady(player2, new RovingKeep());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new BelleOfTheBrawl());
        Permanent first = addCreatureReady(player2, new RovingKeep());
        Permanent second = addCreatureReady(player2, new RovingKeep());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
