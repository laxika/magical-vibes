package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.v.VirtueOfKnowledge;
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

@CardUsed({ElvishArchivist.class, Forest.class, GloriousAnthem.class, Ornithopter.class,
        VirtueOfKnowledge.class})
class ElvishArchivistTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact you control entering puts two +1/+1 counters on Elvish Archivist")
    void allyArtifactEntryPutsTwoCounters() {
        Permanent archivist = harness.addToBattlefieldAndReturn(player1, new ElvishArchivist());
        harness.castFromHand(player1, new Ornithopter(), "{0}");
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
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
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
        harness.castFromHand(player2, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(archivist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void artifactTriggerDoesNotPreventEnchantmentTriggerInSameTurn() {
        Permanent archivist = harness.addToBattlefieldAndReturn(player1, new ElvishArchivist());
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new GloriousAnthem());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(archivist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void enchantmentTriggerDoesNotPreventArtifactTriggerInSameTurn() {
        Permanent archivist = harness.addToBattlefieldAndReturn(player1, new ElvishArchivist());
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        harness.enterBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(archivist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void opponentEnchantmentEntryDoesNotConsumeYourTrigger() {
        harness.addToBattlefield(player1, new ElvishArchivist());
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        harness.enterBattlefieldAndReturn(player2, new GloriousAnthem());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.enterBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void eachArchivistGetsItsOwnArtifactTrigger() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ElvishArchivist());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ElvishArchivist());

        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void artifactAbilityCanTriggerAgainDuringOpponentsTurn() {
        Permanent archivist = harness.addToBattlefieldAndReturn(player1, new ElvishArchivist());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        harness.passBothPriorities();

        assertThat(archivist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void enchantmentAbilityCanTriggerAgainDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new ElvishArchivist());
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void additionalEtbTriggersCannotBypassArtifactOncePerTurnLimit() {
        harness.addToBattlefield(player1, new VirtueOfKnowledge());
        Permanent archivist = harness.addToBattlefieldAndReturn(player1, new ElvishArchivist());

        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(archivist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void additionalEtbTriggersCannotBypassEnchantmentOncePerTurnLimit() {
        harness.addToBattlefield(player1, new VirtueOfKnowledge());
        harness.addToBattlefield(player1, new ElvishArchivist());
        Card drawn = new Forest();
        Card remaining = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn, remaining));

        harness.enterBattlefieldAndReturn(player1, new GloriousAnthem());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }
}
