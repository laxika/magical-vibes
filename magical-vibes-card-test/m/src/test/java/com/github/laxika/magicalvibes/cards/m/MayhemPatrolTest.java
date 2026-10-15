package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RiveteersInitiate;
import com.github.laxika.magicalvibes.cards.s.Strangle;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MayhemPatrol.class, RiveteersInitiate.class, Strangle.class})
class MayhemPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts any target creature by +1/+0 until end of turn")
    void attackTriggerBoostsTargetCreature() {
        addCreatureReady(player1, new MayhemPatrol());
        Permanent target = addCreatureReady(player2, new RiveteersInitiate());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, and sacrifices at the next end step")
    void blitzGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new MayhemPatrol()));
        harness.setLibrary(player1, List.of(new RiveteersInitiate()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent patrol = findPermanent(player1, "Mayhem Patrol");
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mayhem Patrol");
        harness.assertInHand(player1, "Riveteers Initiate");
    }

    @Test
    @DisplayName("Blitz applies haste immediately without an enters-the-battlefield trigger")
    void blitzHasHasteAsSoonAsSpellResolves() {
        harness.setHand(player1, List.of(new MayhemPatrol()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent patrol = findPermanent(player1, "Mayhem Patrol");
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting normally does not grant haste or schedule a sacrifice")
    void normalCastDoesNotApplyBlitz() {
        harness.setHand(player1, List.of(new MayhemPatrol()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent patrol = findPermanent(player1, "Mayhem Patrol");
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Mayhem Patrol")).isSameAs(patrol);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger can target Mayhem Patrol itself")
    void attackTriggerCanTargetItself() {
        Permanent patrol = addCreatureReady(player1, new MayhemPatrol());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, patrol.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, patrol)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, patrol)).isEqualTo(2);
    }

    @Test
    @DisplayName("A blitzed Patrol draws when killed before the end step")
    void blitzDrawsWhenKilled() {
        harness.setHand(player1, List.of(new MayhemPatrol(), new Strangle()));
        harness.setLibrary(player1, List.of(new RiveteersInitiate()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent patrol = findPermanent(player1, "Mayhem Patrol");
        harness.castSorcery(player1, 0, patrol.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mayhem Patrol");
        harness.assertInHand(player1, "Riveteers Initiate");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A normally cast Patrol does not draw when it dies")
    void normalCastDoesNotDrawOnDeath() {
        harness.setHand(player1, List.of(new MayhemPatrol(), new Strangle()));
        harness.setLibrary(player1, List.of(new RiveteersInitiate()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent patrol = findPermanent(player1, "Mayhem Patrol");
        harness.castSorcery(player1, 0, patrol.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mayhem Patrol");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Menace requires two blockers and permits two blockers")
    void menaceRequiresTwoBlockers() {
        Permanent patrol = addCreatureReady(player1, new MayhemPatrol());
        Permanent first = addCreatureReady(player2, new RiveteersInitiate());
        Permanent second = addCreatureReady(player2, new RiveteersInitiate());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, patrol.getId());
        resolveAllTriggers();
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

}
