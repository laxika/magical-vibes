package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CephalidInkshrouder.class, SuntailHawk.class, Chastise.class})
class CephalidInkshrouderTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card grants shroud and makes this creature unblockable")
    void discardGrantsShroudAndUnblockable() {
        Permanent inkshrouder = harness.addToBattlefieldAndReturn(player1, new CephalidInkshrouder());
        harness.setHand(player1, List.of(new SuntailHawk()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Suntail Hawk");
        assertThat(gqs.hasKeyword(gd, inkshrouder, Keyword.SHROUD)).isTrue();
        assertThat(inkshrouder.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Shroud and unblockability wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent inkshrouder = harness.addToBattlefieldAndReturn(player1, new CephalidInkshrouder());
        harness.setHand(player1, List.of(new SuntailHawk()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, inkshrouder, Keyword.SHROUD)).isFalse();
        assertThat(inkshrouder.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Unblockability prevents a creature from blocking this creature")
    void unblockabilityPreventsBlocking() {
        Permanent inkshrouder = addCreatureReady(player1, new CephalidInkshrouder());
        addCreatureReady(player2, new SuntailHawk());
        inkshrouder.setAttacking(true);
        harness.setHand(player1, List.of(new SuntailHawk()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Shroud prevents a spell from targeting this creature")
    void shroudPreventsTargeting() {
        Permanent inkshrouder = addCreatureReady(player1, new CephalidInkshrouder());
        inkshrouder.setAttacking(true);
        harness.setHand(player1, List.of(new SuntailHawk()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Chastise()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, inkshrouder.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.addToBattlefieldAndReturn(player1, new CephalidInkshrouder());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
