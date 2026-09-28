package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AugustaOrderReturned.class, GrizzlyBears.class, HillGiant.class,
        Abolish.class, RhysticCave.class})
class AugustaOrderReturnedTest extends BaseCardTest {

    @Test
    void exilesOneCardFromEachGraveyardAndCountsOnlyNonlands() {
        Permanent augusta = addCreatureReady(player1, new AugustaOrderReturned());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new HillGiant());

        Abolish ownSpell = new Abolish();
        RhysticCave ownLand = new RhysticCave();
        Abolish opponentSpell = new Abolish();
        RhysticCave opponentLand = new RhysticCave();
        harness.setGraveyard(player1, List.of(ownSpell, ownLand));
        harness.setGraveyard(player2, List.of(opponentSpell, opponentLand));

        declareAttackersAt(player1, List.of(augusta, attacker), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(ownSpell.getId(), ownLand.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownSpell.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(opponentSpell.getId(), opponentLand.getId());
        harness.handleMultipleCardsChosen(player2, List.of(opponentSpell.getId()));

        PendingInteraction.PermanentChoice targetChoice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(augusta.getId(), attacker.getId())
                .doesNotContain(nonattacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownSpell);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentSpell);
        harness.assertInGraveyard(player1, "Rhystic Cave");
        harness.assertInGraveyard(player2, "Rhystic Cave");
    }

    @Test
    void doesNotQueueTheCounterAbilityWhenOnlyLandsAreExiled() {
        Permanent augusta = addCreatureReady(player1, new AugustaOrderReturned());
        RhysticCave ownLand = new RhysticCave();
        RhysticCave opponentLand = new RhysticCave();
        harness.setGraveyard(player1, List.of(ownLand));
        harness.setGraveyard(player2, List.of(opponentLand));

        declareAttackersAt(player1, List.of(augusta), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownLand);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentLand);
    }

    private void declareAttackersAt(Player attackerController, List<Permanent> attackers, UUID attackTarget) {
        harness.forceActivePlayer(attackerController);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        List<Permanent> battlefield = gd.playerBattlefields.get(attackerController.getId());
        List<Integer> attackerIndices = attackers.stream().map(battlefield::indexOf).toList();
        Map<Integer, UUID> attackTargets = attackers.stream()
                .collect(java.util.stream.Collectors.toMap(battlefield::indexOf, ignored -> attackTarget));
        harness.inMutationScope(() -> harness.getCombatAttackService().declareAttackers(
                gd, attackerController, attackerIndices, attackTargets));
    }
}
