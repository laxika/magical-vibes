package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShamblingAttendantsTest extends BaseCardTest {

    @Test
    @DisplayName("Delve exiles graveyard cards to pay the generic creature cost")
    void delvePaysGenericCost() {
        List<Card> graveyard = List.of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new ShamblingAttendants()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4, 5, 6));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof ShamblingAttendants);
    }

    @Test
    @DisplayName("Deathtouch destroys a larger blocker in combat")
    void deathtouchDestroysLargerBlocker() {
        Permanent attendant = harness.addToBattlefieldAndReturn(player1, new ShamblingAttendants());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        attendant.setSummoningSick(false);
        attendant.setAttacking(true);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(attendant.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }
}
