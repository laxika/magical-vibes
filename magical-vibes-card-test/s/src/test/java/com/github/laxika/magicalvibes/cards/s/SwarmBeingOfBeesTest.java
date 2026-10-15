package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngryRabble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwarmBeingOfBees.class, AngryRabble.class})
class SwarmBeingOfBeesTest extends BaseCardTest {

    @Test
    @DisplayName("Mayhem casts Swarm from the graveyard for {B} after it was discarded this turn")
    void mayhemCastsAfterDiscarding() {
        SwarmBeingOfBees swarm = new SwarmBeingOfBees();
        harness.setGraveyard(player1, List.of(swarm));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(swarm.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(permanent.getOriginalCard()).isSameAs(swarm);
    }

    @Test
    @DisplayName("Mayhem cannot cast Swarm from the graveyard before it was discarded")
    void mayhemRequiresDiscardThisTurn() {
        harness.setGraveyard(player1, List.of(new SwarmBeingOfBees()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flash allows mayhem during an opponent's end step")
    void mayhemUsesFlashTiming() {
        SwarmBeingOfBees swarm = new SwarmBeingOfBees();
        harness.setGraveyard(player1, List.of(swarm));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(swarm.getId())));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getOriginalCard).containsExactly(swarm);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Flash allows Swarm to be cast normally from hand on an opponent's turn")
    void flashCastsFromHand() {
        SwarmBeingOfBees swarm = new SwarmBeingOfBees();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.castFromHand(player1, swarm, "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getOriginalCard).containsExactly(swarm);
    }

    @Test
    @DisplayName("Mayhem still requires black mana")
    void mayhemCannotBePaidWithColorlessMana() {
        SwarmBeingOfBees swarm = new SwarmBeingOfBees();
        harness.setGraveyard(player1, List.of(swarm));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(swarm.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(swarm);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Discarding another copy does not enable mayhem for Swarm already in the graveyard")
    void mayhemRequiresThisSpecificCardToHaveBeenDiscarded() {
        SwarmBeingOfBees olderSwarm = new SwarmBeingOfBees();
        SwarmBeingOfBees discardedSwarm = new SwarmBeingOfBees();
        harness.setGraveyard(player1, List.of(olderSwarm, discardedSwarm));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(discardedSwarm.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(olderSwarm, discardedSwarm);
    }

    @Test
    @DisplayName("Flying prevents a creature without flying or reach from blocking Swarm")
    void flyingPreventsGroundBlockers() {
        addCreatureReady(player1, new SwarmBeingOfBees());
        addCreatureReady(player2, new AngryRabble());
        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
