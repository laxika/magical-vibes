package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NivixGuildmage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpectrumSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from multicolored sources")
    void hasProtectionFromMulticoloredSources() {
        Permanent sentinel = addCreatureReady(player1, new SpectrumSentinel());
        Permanent multicoloredSource = addCreatureReady(player2, new NivixGuildmage());

        assertThat(gqs.hasProtectionFromSource(gd, sentinel, multicoloredSource)).isTrue();
    }

    @Test
    @DisplayName("Is not protected from monocolored sources")
    void isNotProtectedFromMonocoloredSources() {
        Permanent sentinel = addCreatureReady(player1, new SpectrumSentinel());
        Permanent monocoloredSource = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasProtectionFromSource(gd, sentinel, monocoloredSource)).isFalse();
    }

    @Test
    @DisplayName("Gains 1 life when an opponent's nonbasic land enters")
    void gainsLifeForOpponentNonbasicLand() {
        harness.addToBattlefield(player1, new SpectrumSentinel());
        harness.setLife(player1, 20);
        playLand(player2, new GhostQuarter());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's basic land")
    void doesNotTriggerForOpponentBasicLand() {
        harness.addToBattlefield(player1, new SpectrumSentinel());
        harness.setLife(player1, 20);
        playLand(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger for the controller's nonbasic land")
    void doesNotTriggerForControllerNonbasicLand() {
        harness.addToBattlefield(player1, new SpectrumSentinel());
        harness.setLife(player1, 20);
        playLand(player1, new GhostQuarter());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private void playLand(Player player, Card land) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(land));
        harness.castCreature(player, 0);
    }
}
