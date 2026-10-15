package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronManMasterOfMachines.class, FountainOfYouth.class, Forest.class})
class IronManMasterOfMachinesTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each other artifact you control")
    void countsOtherArtifacts() {
        Permanent ironMan = addCreatureReady(player1, new IronManMasterOfMachines());
        harness.addToBattlefield(player1, new FountainOfYouth());

        assertThat(gqs.getEffectivePower(gd, ironMan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ironMan)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking draws a card when an artifact entered under your control this turn")
    void attackDrawsAfterArtifactEntry() {
        addCreatureReady(player1, new IronManMasterOfMachines());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Attacking does not draw when no artifact entered under your control this turn")
    void attackDoesNotDrawWithoutArtifactEntry() {
        addCreatureReady(player1, new IronManMasterOfMachines());
        Forest topCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void ignoresOpponentsArtifactsAndNonartifactsForPower() {
        Permanent ironMan = addCreatureReady(player1, new IronManMasterOfMachines());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, ironMan)).isEqualTo(1);
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new FountainOfYouth());
        assertThat(gqs.getEffectivePower(gd, ironMan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ironMan)).isEqualTo(4);
    }

    @Test
    void opponentArtifactEntryDoesNotEnableDraw() {
        addCreatureReady(player1, new IronManMasterOfMachines());
        harness.setHand(player1, List.of());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.enterBattlefieldAndReturn(player2, new FountainOfYouth());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void nonartifactEntryDoesNotEnableDraw() {
        addCreatureReady(player1, new IronManMasterOfMachines());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleArtifactEntriesStillDrawOnlyOneCard() {
        addCreatureReady(player1, new IronManMasterOfMachines());
        harness.setHand(player1, List.of());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void ironMansOwnEntryEnablesDraw() {
        Permanent ironMan = harness.enterBattlefieldAndReturn(player1, new IronManMasterOfMachines());
        ironMan.setSummoningSick(false);
        harness.setHand(player1, List.of());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void artifactEntryStillCountsAfterArtifactLeavesBattlefield() {
        Permanent ironMan = addCreatureReady(player1, new IronManMasterOfMachines());
        harness.setHand(player1, List.of());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent artifact = harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerGraveyards.get(player1.getId()).add(artifact.getCard());

        assertThat(gqs.getEffectivePower(gd, ironMan)).isEqualTo(1);
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
    }
}
