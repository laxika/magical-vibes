package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CleverImpersonator;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DragonsEyeSavantsTest extends BaseCardTest {

    @Test
    void morphRevealsBlueCardAndKeepsItInHand() {
        CleverImpersonator blueCard = new CleverImpersonator();
        harness.setHand(player1, List.of(new DragonsEyeSavants(), blueCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0, 1);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Dragon's Eye Savants");
        assertThat(permanent.isFaceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(blueCard);
    }

    @Test
    void turningFaceUpLooksAtTargetOpponentsHand() {
        harness.setHand(player2, List.of(new CleverImpersonator()));
        harness.setHand(player1, List.of(new DragonsEyeSavants(), new CleverImpersonator()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0, 1);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Dragon's Eye Savants");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(permanent));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(permanent.isFaceDown()).isFalse();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"));
    }
}
