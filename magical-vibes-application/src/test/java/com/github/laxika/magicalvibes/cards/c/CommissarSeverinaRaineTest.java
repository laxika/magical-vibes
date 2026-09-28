package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommissarSeverinaRaine.class, GrizzlyBears.class, Forest.class})
class CommissarSeverinaRaineTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with other creatures makes each opponent lose life for each of them")
    void attacksDrainForOtherAttackers() {
        Permanent commissar = addCreatureReady(player1, new CommissarSeverinaRaine());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent thirdBlocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(commissar),
                gd.playerBattlefields.get(player1.getId()).indexOf(firstBear),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondBear)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(commissar)),
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(firstBear)),
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(thirdBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(secondBear))));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Sacrificing another creature gains life and draws a card")
    void sacrificesAnotherCreatureGainsLifeAndDraws() {
        harness.addToBattlefield(player1, new CommissarSeverinaRaine());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Commissar Severina Raine");
    }
}
