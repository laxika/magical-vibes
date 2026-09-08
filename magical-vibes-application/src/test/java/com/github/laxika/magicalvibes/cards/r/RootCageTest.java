package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DauthiMercenary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RootCageTest extends BaseCardTest {

    @Test
    @DisplayName("Mercenaries stay tapped through their controller's untap step")
    void mercenariesDontUntap() {
        addReady(player1, new RootCage());
        Permanent mercenary = addReady(player1, new DauthiMercenary());
        mercenary.tap();

        advanceToNextTurn(player2);

        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-Mercenary creatures untap normally")
    void nonMercenariesUntap() {
        addReady(player1, new RootCage());
        Permanent creature = addReady(player1, new GrizzlyBears());
        creature.tap();

        advanceToNextTurn(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Root Cage affects opponents' Mercenaries")
    void affectsOpponentMercenaries() {
        addReady(player1, new RootCage());
        Permanent mercenary = addReady(player2, new DauthiMercenary());
        mercenary.tap();

        advanceToNextTurn(player1);

        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mercenaries untap after Root Cage leaves the battlefield")
    void untapsAfterRootCageLeaves() {
        Permanent rootCage = addReady(player1, new RootCage());
        Permanent mercenary = addReady(player1, new DauthiMercenary());
        mercenary.tap();
        gd.playerBattlefields.get(player1.getId()).remove(rootCage);

        advanceToNextTurn(player2);

        assertThat(mercenary.isTapped()).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
