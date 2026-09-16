package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GuidedStrike;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CephalidInkshrouder.class, GuidedStrike.class, SuntailHawk.class})
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
    @DisplayName("Shroud prevents spells from targeting this creature")
    void shroudPreventsTargeting() {
        Permanent inkshrouder = harness.addToBattlefieldAndReturn(player1, new CephalidInkshrouder());
        harness.setHand(player1, List.of(new SuntailHawk()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GuidedStrike()));
        harness.setLibrary(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, inkshrouder.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Guided Strike");
    }

    @Test
    @DisplayName("Unblockability prevents a creature from blocking this creature")
    void unblockabilityPreventsBlocking() {
        Permanent inkshrouder = addCreatureReady(player1, new CephalidInkshrouder());
        Permanent blocker = addCreatureReady(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SuntailHawk()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        inkshrouder.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(inkshrouder);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
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
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, inkshrouder, Keyword.SHROUD)).isFalse();
        assertThat(inkshrouder.isCantBeBlocked()).isFalse();
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
