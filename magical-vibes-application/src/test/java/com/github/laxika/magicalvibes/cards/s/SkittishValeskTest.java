package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SkittishValesk.class)
class SkittishValeskTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new SkittishValesk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent valesk = findPermanent(player1, "Skittish Valesk");
        assertThat(valesk.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(valesk));
        harness.passBothPriorities();

        assertThat(valesk.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("At the beginning of its controller's upkeep, it turns face down on a lost flip")
    void turnsFaceDownOnLostUpkeepFlip() {
        harness.addToBattlefield(player1, new SkittishValesk());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        Permanent valesk = findPermanent(player1, "Skittish Valesk");
        boolean lostFlip = gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("loses the coin flip for Skittish Valesk"));
        assertThat(valesk.isFaceDown()).isEqualTo(lostFlip);
    }
}
