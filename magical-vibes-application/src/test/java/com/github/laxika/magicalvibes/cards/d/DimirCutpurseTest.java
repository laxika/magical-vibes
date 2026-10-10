package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinSpelunkers;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DimirCutpurse.class, Forest.class, GoblinSpelunkers.class, LastGasp.class})
class DimirCutpurseTest extends BaseCardTest {

    @Test
    void combatDamageMakesPlayerDiscardAndControllerDraw() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GoblinSpelunkers(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        addAttackingCutpurse(player1);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void controllerDrawsWhenOpponentControlsTheCutpurse() {
        harness.setHand(player1, List.of(new GoblinSpelunkers(), new Forest()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        addAttackingCutpurse(player2);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void emptyHandStillAllowsControllerToDraw() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        addAttackingCutpurse(player1);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void blockedCutpurseDoesNotTrigger() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        addAttackingCutpurse(player1);
        Permanent blocker = addCreatureReady(player2, new GoblinSpelunkers());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void triggerSurvivesSourceRemovalAndDrawWaitsForChosenDiscard() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new LastGasp(), new GoblinSpelunkers(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent cutpurse = addAttackingCutpurse(player1);
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.COMBAT_DAMAGE, TurnStep.END_OF_COMBAT));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.COMBAT_DAMAGE, TurnStep.END_OF_COMBAT));

        resolveCombat();
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, cutpurse.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cutpurse.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).singleElement().isInstanceOf(GoblinSpelunkers.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(Forest.class::isInstance);
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent addAttackingCutpurse(com.github.laxika.magicalvibes.model.Player player) {
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.END_OF_COMBAT));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.END_OF_COMBAT));
        Permanent cutpurse = addCreatureReady(player, new DimirCutpurse());
        cutpurse.setAttacking(true);
        return cutpurse;
    }
}
