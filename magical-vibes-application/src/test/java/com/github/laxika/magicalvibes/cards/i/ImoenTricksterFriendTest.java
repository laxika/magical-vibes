package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImoenTricksterFriend.class, GrizzlyBears.class, Plains.class, Island.class, Shock.class, DoomBlade.class})
class ImoenTricksterFriendTest extends BaseCardTest {

    @Test
    void cannotBeBlockedWhenAttackingAlone() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent imoen = addCreatureReady(player1, new ImoenTricksterFriend());
        imoen.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(imoen);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void reducedSpecializeExilesAnInstantAndPutsCountersOnCreatures() {
        Permanent imoen = addCreatureReady(player1, new ImoenTricksterFriend());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Shock shock = new Shock();
        DoomBlade doomBlade = new DoomBlade();
        harness.setGraveyard(player1, List.of(shock, doomBlade));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(imoen.getCard().getName()).isEqualTo("Imoen, Honorable Trickster");
        assertThat(imoen.getCard().getPower()).isEqualTo(3);

        imoen.setAttacking(true);
        imoen.setAttackTarget(player2.getId());
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getId())
                .contains(shock.getId());
        assertThat(imoen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void blueFaceTargetsAnOpponentsCreatureAfterExiling() {
        Permanent imoen = addCreatureReady(player1, new ImoenTricksterFriend());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock, new DoomBlade()));
        harness.setHand(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        imoen.setAttacking(true);
        imoen.setAttackTarget(player2.getId());
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(blocker.isTapped()).isTrue();
    }
}
