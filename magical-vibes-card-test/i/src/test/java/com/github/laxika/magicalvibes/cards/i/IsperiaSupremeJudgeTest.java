package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.j.JaceArchitectOfThought;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IsperiaSupremeJudge.class, DrudgeBeetle.class, Forest.class, JaceArchitectOfThought.class})
class IsperiaSupremeJudgeTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the draw when a creature attacks you")
    void acceptingDrawWhenAttacked() {
        addIsperia(player1);
        addCreatureReady(player2, new DrudgeBeetle());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player2, List.of(0), null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining the draw draws no card")
    void decliningDrawsNoCard() {
        addIsperia(player1);
        addCreatureReady(player2, new DrudgeBeetle());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player2, List.of(0), null);
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Attacking a planeswalker you control also offers the draw")
    void attackingPlaneswalkerOffersDraw() {
        addIsperia(player1);
        Permanent planeswalker = addPlaneswalker(player1, 4);
        addCreatureReady(player2, new DrudgeBeetle());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger when your creature attacks an opponent")
    void ownAttackDoesNotTrigger() {
        addIsperia(player1);
        addCreatureReady(player1, new DrudgeBeetle());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(1), null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Each attacking creature offers a separate optional draw")
    void multipleAttackersOfferIndependentDraws() {
        addIsperia(player1);
        Permanent planeswalker = addPlaneswalker(player1, 4);
        addCreatureReady(player2, new DrudgeBeetle());
        addCreatureReady(player2, new DrudgeBeetle());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player2, List.of(0, 1),
                Map.of(0, player1.getId(), 1, planeswalker.getId()));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The draw still resolves after Isperia leaves the battlefield")
    void drawResolvesWithoutIsperia() {
        addIsperia(player1);
        addCreatureReady(player2, new DrudgeBeetle());
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player2, List.of(0), null);
        Permanent isperia = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(isperia.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private void addIsperia(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new IsperiaSupremeJudge());
        perm.setSummoningSick(false);
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new JaceArchitectOfThought());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }
}
