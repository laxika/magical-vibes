package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrivateResearchTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger puts a page counter on Private Research")
    void upkeepAcceptedAddsPageCounter() {
        Permanent research = addResearchAttachedTo(player1, player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(research.getCounterCount(CounterType.PAGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves page counters unchanged")
    void upkeepDeclinedAddsNoPageCounter() {
        Permanent research = addResearchAttachedTo(player1, player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(research.getCounterCount(CounterType.PAGE)).isZero();
    }

    @Test
    @DisplayName("When the enchanted creature dies, Private Research draws for each page counter")
    void enchantedCreatureDeathDrawsForEachPageCounter() {
        Permanent research = addResearchAttachedTo(player1, player1);
        research.setCounterCount(CounterType.PAGE, 2);
        Permanent creature = findAttachedCreature(research);
        int handSize = gd.playerHands.get(player1.getId()).size();

        killWithShock(creature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
        harness.assertInGraveyard(player1, "Private Research");
    }

    private Permanent addResearchAttachedTo(Player auraController, Player creatureController) {
        Permanent creature = harness.addToBattlefieldAndReturn(creatureController, new GrizzlyBears());
        Permanent research = new Permanent(new PrivateResearch());
        research.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(research);
        return research;
    }

    private Permanent findAttachedCreature(Permanent aura) {
        return gd.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> aura.getId().equals(permanent.getId())
                        || aura.getAttachedTo().equals(permanent.getId()))
                .filter(permanent -> !permanent.getId().equals(aura.getId()))
                .findFirst()
                .orElseThrow();
    }

    private void killWithShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
