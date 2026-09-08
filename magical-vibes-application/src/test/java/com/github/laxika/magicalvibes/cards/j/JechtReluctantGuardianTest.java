package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BraskasFinalAeon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JechtReluctantGuardian.class, BraskasFinalAeon.class, GrizzlyBears.class})
class JechtReluctantGuardianTest extends BaseCardTest {

    @Test
    void combatDamageMayTransformAndResolveChapterOne() {
        Permanent jecht = addCreatureReady(player1, new JechtReluctantGuardian());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        jecht.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent transformed = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BraskasFinalAeon)
                .findFirst()
                .orElseThrow();
        assertThat(transformed.isTransformed()).isTrue();
        assertThat(transformed.getCounterCount(CounterType.LORE)).isEqualTo(1);

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void combatDamageMayBeDeclined() {
        Permanent jecht = addCreatureReady(player1, new JechtReluctantGuardian());
        jecht.setAttacking(true);

        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(jecht.isTransformed()).isFalse();
        harness.assertOnBattlefield(player1, "Jecht, Reluctant Guardian");
    }

    @Test
    void chapterThreeMakesEachOpponentSacrificeTwoCreatures() {
        JechtReluctantGuardian front = new JechtReluctantGuardian();
        Permanent braska = new Permanent(front);
        braska.setCard(front.getBackFaceCard());
        braska.setTransformed(true);
        braska.setSummoningSick(false);
        braska.setCounterCount(CounterType.LORE, 2);
        gd.playerBattlefields.get(player1.getId()).add(braska);

        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent third = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).contains(first.getId(), second.getId(), third.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(third);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(braska);
    }
}
