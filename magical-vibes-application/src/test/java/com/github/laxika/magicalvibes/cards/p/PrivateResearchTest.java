package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.cards.r.RecklessAbandon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrivateResearch.class, GoliathBeetle.class, RecklessAbandon.class, BraidwoodCup.class})
class PrivateResearchTest extends BaseCardTest {

    @Test
    @DisplayName("Private Research resolves attached to a target creature")
    void resolvesAttachedToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());
        harness.setHand(player1, List.of(new PrivateResearch()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof PrivateResearch
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Private Research cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BraidwoodCup());
        harness.setHand(player1, List.of(new PrivateResearch()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

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
    @DisplayName("The Aura controller gets the upkeep trigger even when another player controls the creature")
    void upkeepTriggerUsesAuraController() {
        Permanent research = addResearchAttachedTo(player1, player2);

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

        killWithRecklessAbandon(creature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
        harness.assertInGraveyard(player1, "Private Research");
    }

    @Test
    @DisplayName("When an opponent controls the enchanted creature, the Aura controller draws")
    void enchantedCreatureDeathDrawsForAuraController() {
        Permanent research = addResearchAttachedTo(player1, player2);
        research.setCounterCount(CounterType.PAGE, 2);
        Permanent creature = findAttachedCreature(research);
        int auraControllerHandSize = gd.playerHands.get(player1.getId()).size();

        killWithRecklessAbandon(creature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(auraControllerHandSize + 2);
        harness.assertInGraveyard(player1, "Private Research");
    }

    @Test
    @DisplayName("A creature other than the enchanted one does not trigger Private Research")
    void unrelatedCreatureDeathDoesNotDraw() {
        Permanent research = addResearchAttachedTo(player1, player1);
        research.setCounterCount(CounterType.PAGE, 2);
        Permanent unrelatedCreature = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());
        int handSize = gd.playerHands.get(player1.getId()).size();

        killWithRecklessAbandon(unrelatedCreature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(research);
    }

    private Permanent addResearchAttachedTo(Player auraController, Player creatureController) {
        Permanent creature = harness.addToBattlefieldAndReturn(creatureController, new GoliathBeetle());
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

    private void killWithRecklessAbandon(Permanent target) {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new GoliathBeetle());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new RecklessAbandon()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player2, 0, target.getId(), sacrifice.getId());
        resolveAllTriggers();
    }
}
