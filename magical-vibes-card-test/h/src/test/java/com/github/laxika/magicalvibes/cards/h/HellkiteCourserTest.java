package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrenzoHavocRaiser;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HellkiteCourser.class, GrenzoHavocRaiser.class})
class HellkiteCourserTest extends BaseCardTest {

    @Test
    void mayPutCommanderOntoBattlefieldWithHasteUntilNextEndStep() {
        Card commander = commander();
        Permanent hellkite = harness.enterBattlefieldAndReturn(player1, new HellkiteCourser());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteredCommander = findPermanentByCardId(commander.getId());
        assertThat(enteredCommander.isCommander()).isTrue();
        assertThat(gqs.hasKeyword(gd, enteredCommander, Keyword.HASTE)).isTrue();
        assertThat(gd.playerCommandZones.get(player1.getId())).isEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanentByCardIdOrNull(commander.getId())).isNull();
        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(commander);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hellkite);
    }

    @Test
    void mayDeclineCommanderEntry() {
        Card commander = commander();
        harness.enterBattlefieldAndReturn(player1, new HellkiteCourser());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanentByCardIdOrNull(commander.getId())).isNull();
        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(commander);
    }

    @Test
    void puttingCommanderOntoBattlefieldDoesNotCountAsCastingIt() {
        Card commander = commander();
        gd.commanderTaxByCardId.put(commander.getId(), 4);
        harness.enterBattlefieldAndReturn(player1, new HellkiteCourser());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanentByCardId(commander.getId())).isNotNull();
        assertThat(gd.commanderTaxByCardId.get(commander.getId())).isEqualTo(4);
        assertThat(gd.commanderCastsFromCommandZoneThisGame.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void doesNothingWhenControllerHasNoCommanderInCommandZone() {
        Card opposingCommander = new GrenzoHavocRaiser();
        opposingCommander.setOwnerId(player2.getId());
        gd.makeCommander(player2.getId(), opposingCommander);
        gd.playerCommandZones.put(player2.getId(), new ArrayList<>(List.of(opposingCommander)));
        harness.enterBattlefieldAndReturn(player1, new HellkiteCourser());

        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerCommandZones.get(player2.getId())).containsExactly(opposingCommander);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void delayedReturnHasHellkiteCourserAsItsSource() {
        Card commander = commander();
        HellkiteCourser courser = new HellkiteCourser();
        harness.enterBattlefieldAndReturn(player1, courser);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(findPermanentByCardIdOrNull(commander.getId())).isNotNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(courser);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();
    }

    @Test
    void enteringDuringEndStepReturnsCommanderAtFollowingEndStep() {
        Card commander = commander();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.enterBattlefieldAndReturn(player1, new HellkiteCourser());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(findPermanentByCardIdOrNull(commander.getId())).isNotNull();
        assertThat(gqs.hasKeyword(gd, findPermanentByCardId(commander.getId()), Keyword.HASTE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanentByCardIdOrNull(commander.getId())).isNull();
        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(commander);
    }

    @Test
    void delayedReturnDoesNotMoveCommanderThatAlreadyLeftBattlefield() {
        Card commander = commander();
        harness.enterBattlefieldAndReturn(player1, new HellkiteCourser());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        Permanent enteredCommander = findPermanentByCardId(commander.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToCommandZone(gd, enteredCommander));
        gd.playerCommandZones.get(player1.getId()).remove(commander);
        Permanent newCommander = harness.enterBattlefieldAndReturn(player1, commander);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(newCommander);
        assertThat(gd.playerCommandZones.get(player1.getId())).isEmpty();
        assertThat(gqs.hasKeyword(gd, newCommander, Keyword.HASTE)).isFalse();
    }

    private Card commander() {
        Card commander = new GrenzoHavocRaiser();
        commander.setOwnerId(player1.getId());
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        return commander;
    }

    private Permanent findPermanentByCardId(java.util.UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }

    private Permanent findPermanentByCardIdOrNull(java.util.UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}
