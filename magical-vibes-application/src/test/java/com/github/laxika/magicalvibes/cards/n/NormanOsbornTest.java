package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NormanOsborn.class, GreenGoblin.class, GrizzlyBears.class, MindStone.class, Mountain.class})
class NormanOsbornTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked by creatures")
    void cannotBeBlocked() {
        Permanent norman = addFrontReady(player1);
        norman.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Transforms into the back face at sorcery speed")
    void transformsIntoBackFace() {
        Permanent norman = addFrontReady(player1);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(norman.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Connives after dealing combat damage to a player")
    void connivesOnCombatDamage() {
        Permanent norman = addFrontReady(player1);
        Mountain mountain = new Mountain();
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(mountain)));
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(drawn));

        assertThat(norman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(mountain);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Mayhem casts a nonland card discarded this turn for two less")
    void mayhemCastsDiscardedCardWithReduction() {
        addBackReady(player1);
        MindStone stone = new MindStone();
        harness.setGraveyard(player1, List.of(stone));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(stone.getId())));
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getId().equals(stone.getId()));
    }

    @Test
    @DisplayName("Mayhem requires the card to have been discarded this turn")
    void mayhemRequiresDiscardThisTurn() {
        addBackReady(player1);
        harness.setGraveyard(player1, List.of(new MindStone()));
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addFrontReady(Player player) {
        return addCreatureReady(player, new NormanOsborn());
    }

    private Permanent addBackReady(Player player) {
        NormanOsborn card = new NormanOsborn();
        Permanent permanent = addCreatureReady(player, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
