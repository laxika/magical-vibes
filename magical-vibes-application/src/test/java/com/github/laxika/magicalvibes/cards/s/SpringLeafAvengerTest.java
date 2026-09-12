package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpringLeafAvenger.class, GrizzlyBears.class, GiantGrowth.class})
class SpringLeafAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage returns a target permanent card from the graveyard")
    void combatDamageReturnsTargetPermanentCard() {
        GrizzlyBears permanentCard = new GrizzlyBears();
        GiantGrowth nonPermanentCard = new GiantGrowth();
        harness.setGraveyard(player1, List.of(permanentCard, nonPermanentCard));
        Permanent avenger = addCreatureReady(player1, new SpringLeafAvenger());
        avenger.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(permanentCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(permanentCard.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Giant Growth");
    }

    @Test
    @DisplayName("Ninjutsu puts Spring-Leaf Avenger onto the battlefield tapped and attacking")
    void ninjutsu() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SpringLeafAvenger()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent avenger = findPermanent(player1, "Spring-Leaf Avenger");
        assertThat(avenger.isTapped()).isTrue();
        assertThat(avenger.isAttacking()).isTrue();
        assertThat(avenger.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Blocked combat damage does not trigger the graveyard return")
    void blockedCombatDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent avenger = addCreatureReady(player1, new SpringLeafAvenger());
        avenger.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
