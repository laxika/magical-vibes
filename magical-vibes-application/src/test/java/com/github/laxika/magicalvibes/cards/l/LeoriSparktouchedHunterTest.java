package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeoriSparktouchedHunter.class, JaceBeleren.class, GarrukWildspeaker.class})
class LeoriSparktouchedHunterTest extends BaseCardTest {

    @Test
    void combatDamageChoosesAPlaneswalkerType() {
        Permanent leori = addCreatureReady(player1, new LeoriSparktouchedHunter());
        leori.setAttackTarget(player2.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "JACE");

        assertThat(leori.getChosenSubtype()).isEqualTo(CardSubtype.JACE);
        assertThat(gd.temporaryChosenSubtypePermanentIds).contains(leori.getId());
    }

    @Test
    void copiesAnActivatedAbilityOfTheChosenPlaneswalkerType() {
        Permanent leori = addCreatureReady(player1, new LeoriSparktouchedHunter());
        leori.setChosenSubtype(CardSubtype.JACE);
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 5);

        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    void doesNotCopyAnActivatedAbilityOfAnotherPlaneswalkerType() {
        Permanent leori = addCreatureReady(player1, new LeoriSparktouchedHunter());
        leori.setChosenSubtype(CardSubtype.JACE);
        Permanent garruk = harness.addToBattlefieldAndReturn(player1, new GarrukWildspeaker());
        garruk.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Beast")).hasSize(1);
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void chosenPlaneswalkerTypeExpiresAtCleanup() {
        Permanent leori = addCreatureReady(player1, new LeoriSparktouchedHunter());
        leori.setChosenSubtype(CardSubtype.JACE);
        gd.temporaryChosenSubtypePermanentIds.add(leori.getId());

        harness.inMutationScope(() -> GameTestEngineContext.get()
                .getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(leori.getChosenSubtype()).isNull();
        assertThat(gd.temporaryChosenSubtypePermanentIds).isEmpty();
    }

    @Test
    void delayedCopySurvivesLeoriLeavingTheBattlefield() {
        Permanent leori = addCreatureReady(player1, new LeoriSparktouchedHunter());
        dealCombatDamageAndChooseType(leori, "JACE");
        gd.playerBattlefields.get(player1.getId()).remove(leori);
        gd.playerGraveyards.get(player1.getId()).add(leori.getCard());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 5);

        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    void choosingTheSameTypeTwiceCreatesTwoCopies() {
        Permanent leori = addCreatureReady(player1, new LeoriSparktouchedHunter());
        dealCombatDamageAndChooseType(leori, "JACE");
        dealCombatDamageAndChooseType(leori, "JACE");
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 5);

        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 3);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    void choosingAnotherTypeDoesNotEraseTheEarlierDelayedTrigger() {
        Permanent leori = addCreatureReady(player1, new LeoriSparktouchedHunter());
        dealCombatDamageAndChooseType(leori, "JACE");
        dealCombatDamageAndChooseType(leori, "GARRUK");
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 5);

        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
    }

    @Test
    void doesNotCopyBeforeCombatDamageHasResolved() {
        addCreatureReady(player1, new LeoriSparktouchedHunter());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 5);

        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    private void dealCombatDamageAndChooseType(Permanent leori, String type) {
        leori.setAttacking(false);
        leori.setAttackTarget(player2.getId());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.handleListChoice(player1, type);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
    }
}
