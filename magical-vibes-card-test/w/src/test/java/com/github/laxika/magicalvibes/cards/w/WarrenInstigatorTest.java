package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CliffThreader;
import com.github.laxika.magicalvibes.cards.g.GoblinBushwhacker;
import com.github.laxika.magicalvibes.cards.s.SoulsFire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarrenInstigator.class, GoblinBushwhacker.class, CliffThreader.class, SoulsFire.class})
class WarrenInstigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing combat damage offers a Goblin creature from hand")
    void dealsDamageAndOffersGoblinCreature() {
        harness.setHand(player1, List.of(new GoblinBushwhacker()));
        attackAndResolveOneTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Goblin Bushwhacker");
    }

    @Test
    @DisplayName("Only Goblin creature cards are offered")
    void offersOnlyGoblinCreatures() {
        harness.setHand(player1, List.of(new CliffThreader(), new GoblinBushwhacker()));
        attackAndResolveOneTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Declining leaves the Goblin creature in hand")
    void decliningLeavesGoblinInHand() {
        harness.setHand(player1, List.of(new GoblinBushwhacker()));
        attackAndResolveOneTrigger();

        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player1, "Goblin Bushwhacker");
        harness.assertNotOnBattlefield(player1, "Goblin Bushwhacker");
    }

    @Test
    @DisplayName("No Goblin creature in hand does not prompt")
    void noGoblinCreatureDoesNotPrompt() {
        harness.setHand(player1, List.of(new CliffThreader()));
        attackAndResolveOneTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
        harness.assertInHand(player1, "Cliff Threader");
    }

    @Test
    void doubleStrikeCanPutOneGoblinInEachDamageStep() {
        harness.setHand(player1, List.of(new GoblinBushwhacker(), new GoblinBushwhacker()));
        attackAndResolveOneTrigger();

        harness.handleCardChosen(player1, 0);
        assertThat(countPermanents(player1, "Goblin Bushwhacker")).isEqualTo(1);
        harness.passBothPriorities();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(countPermanents(player1, "Goblin Bushwhacker")).isEqualTo(2);
        harness.assertLife(player2, 18);
        assertThat(findPermanents(player1, "Goblin Bushwhacker"))
                .allSatisfy(permanent -> {
                    assertThat(permanent.isTapped()).isFalse();
                    assertThat(permanent.isAttacking()).isFalse();
                });
    }

    @Test
    void noncombatDamageToOpponentOffersGoblin() {
        var instigator = harness.addToBattlefieldAndReturn(player1, new WarrenInstigator());
        harness.setHand(player1, List.of(new SoulsFire(), new GoblinBushwhacker()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(instigator.getId(), player2.getId()));
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Goblin Bushwhacker");
    }

    @Test
    void noncombatDamageToControllerDoesNotTrigger() {
        var instigator = harness.addToBattlefieldAndReturn(player1, new WarrenInstigator());
        harness.setHand(player1, List.of(new SoulsFire(), new GoblinBushwhacker()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(instigator.getId(), player1.getId()));
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Goblin Bushwhacker");
        harness.assertNotOnBattlefield(player1, "Goblin Bushwhacker");
    }

    private void attackAndResolveOneTrigger() {
        addCreatureReady(player1, new WarrenInstigator());
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
    }
}
