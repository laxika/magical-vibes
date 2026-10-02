package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Threaten;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AliciaMastersSkilledSculptor.class, Shock.class, GrizzlyBears.class, Forest.class, Threaten.class})
class AliciaMastersSkilledSculptorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure at the beginning of combat after casting a noncreature spell")
    void createsTreasureAfterCastingNoncreatureSpell() {
        harness.addToBattlefield(player1, new AliciaMastersSkilledSculptor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        advanceToBeginningOfCombat(player1);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Treasure when only a creature spell was cast")
    void doesNotCreateTreasureAfterCastingOnlyCreatureSpell() {
        harness.addToBattlefield(player1, new AliciaMastersSkilledSculptor());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        advanceToBeginningOfCombat(player1);

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("At your end step, each player regains the creatures they own")
    void returnsOwnedCreaturesAtControllerEndStep() {
        harness.addToBattlefield(player1, new AliciaMastersSkilledSculptor());
        Permanent player1Creature = addStolenPermanent(player2, player1, new GrizzlyBears());
        Permanent player2Creature = addStolenPermanent(player1, player2, new GrizzlyBears());
        Permanent player1Land = addStolenPermanent(player2, player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Creature)
                .doesNotContain(player2Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Creature)
                .doesNotContain(player1Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player1Land);
    }

    @Test
    @DisplayName("Does not trigger when no noncreature spell was cast before combat")
    void doesNotTriggerWithoutCastingASpell() {
        harness.addToBattlefield(player1, new AliciaMastersSkilledSculptor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not satisfy the combat condition")
    void opponentSpellDoesNotCreateTreasure() {
        harness.addToBattlefield(player1, new AliciaMastersSkilledSculptor());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        advanceToBeginningOfCombat(player1);

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Does not return creatures during the opponent's end step")
    void doesNotReturnCreaturesAtOpponentEndStep() {
        harness.addToBattlefield(player1, new AliciaMastersSkilledSculptor());
        Permanent creature = addStolenPermanent(player2, player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("The owner keeps a temporarily regained creature after the end step")
    void ownerKeepsTemporarilyRegainedCreatureAfterCleanup() {
        harness.addToBattlefield(player1, new AliciaMastersSkilledSculptor());
        Permanent creature = addStolenPermanent(player2, player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Threaten()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }

    private Permanent addStolenPermanent(Player controller, Player owner, Card card) {
        card.setOwnerId(owner.getId());
        Permanent permanent = harness.addToBattlefieldAndReturn(owner, card);
        gd.stolenCreatures.put(permanent.getId(), owner.getId());
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, controller.getId(), permanent,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));
        return permanent;
    }
}
