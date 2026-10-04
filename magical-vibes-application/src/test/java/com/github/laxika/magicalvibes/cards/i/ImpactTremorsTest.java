package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DragonFodder;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImpactTremors.class, GrizzlyBears.class, DragonFodder.class, Naturalize.class, Opalescence.class})
class ImpactTremorsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each opponent when a creature you control enters")
    void damagesEachOpponentWhenOwnCreatureEnters() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ImpactTremors());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not trigger when a creature enters under an opponent's control")
    void doesNotTriggerForOpponentCreature() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ImpactTremors());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each creature token entering simultaneously triggers separately")
    void triggersForEachToken() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ImpactTremors());

        harness.castFromHand(player1, new DragonFodder(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple copies each trigger for each entering creature")
    void multipleCopiesTriggerIndependently() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ImpactTremors());
        harness.addToBattlefield(player1, new ImpactTremors());

        harness.castFromHand(player1, new DragonFodder(), "{1}{R}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(4);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Queued triggers resolve after Impact Tremors is destroyed")
    void triggersSurviveSourceRemoval() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        var tremors = harness.addToBattlefieldAndReturn(player1, new ImpactTremors());

        harness.castFromHand(player1, new DragonFodder(), "{1}{R}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, tremors.getId());
        harness.assertInGraveyard(player1, "Impact Tremors");
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An enchantment entering does not trigger Impact Tremors")
    void noncreatureEntryDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ImpactTremors());

        harness.castFromHand(player1, new ImpactTremors(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A second-player controller damages the first player for creature tokens")
    void secondPlayerControllerDamagesFirstPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new ImpactTremors());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new DragonFodder(), "{1}{R}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Impact Tremors triggers for its own entry when Opalescence makes it a creature")
    void triggersForOwnEntryWhenAnimated() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Opalescence());

        harness.castFromHand(player1, new ImpactTremors(), "{1}{R}");
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, findPermanent(player1, "Impact Tremors"))).isTrue();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
