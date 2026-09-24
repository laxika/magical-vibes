package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
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

        declareAttackers(List.of(0));
        resolveCombat();
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
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(9);
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
}
