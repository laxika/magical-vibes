package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvishArchivist.class, Forest.class, GloriousAnthem.class, Ornithopter.class})
class ElvishArchivistTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact you control entering puts two +1/+1 counters on Elvish Archivist")
    void allyArtifactEntryPutsTwoCounters() {
        Permanent archivist = harness.addToBattlefieldAndReturn(player1, new ElvishArchivist());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(archivist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The artifact ability triggers only once each turn")
    void allyArtifactEntryTriggersOnlyOnceEachTurn() {
        Permanent archivist = harness.addToBattlefieldAndReturn(player1, new ElvishArchivist());
        harness.setHand(player1, List.of(new Ornithopter(), new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(archivist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An enchantment you control entering draws a card")
    void allyEnchantmentEntryDrawsCard() {
        harness.addToBattlefield(player1, new ElvishArchivist());
        Card drawn = new Forest();
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("The enchantment ability triggers only once each turn")
    void allyEnchantmentEntryTriggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new ElvishArchivist());
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        harness.setHand(player1, List.of(new GloriousAnthem(), new GloriousAnthem()));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw).doesNotContain(secondDraw);
    }

    @Test
    @DisplayName("Artifacts entering under an opponent's control do not trigger it")
    void opponentArtifactEntryDoesNotTrigger() {
        Permanent archivist = harness.addToBattlefieldAndReturn(player1, new ElvishArchivist());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Ornithopter()));

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(archivist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
