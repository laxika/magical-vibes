package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CleansingNova;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({PoisonTipArcher.class, GreenwoodSentinel.class, LightningStrike.class, Shock.class,
        CleansingNova.class})
class PoisonTipArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature dying makes each opponent lose 1 life")
    void anotherCreatureDeathMakesEachOpponentLoseLife() {
        harness.addToBattlefield(player1, new PoisonTipArcher());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID sentinelId = harness.getPermanentId(player1, "Greenwood Sentinel");
        harness.castAndResolveInstant(player2, 0, sentinelId);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Poison-Tip Archer does not trigger when it dies")
    void ownDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new PoisonTipArcher());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        UUID archerId = harness.getPermanentId(player1, "Poison-Tip Archer");
        harness.castAndResolveInstant(player2, 0, archerId);
        harness.assertInGraveyard(player1, "Poison-Tip Archer");

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opposing creature dying also triggers the Archer")
    void opposingCreatureDeathTriggers() {
        harness.addToBattlefield(player1, new PoisonTipArcher());
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Greenwood Sentinel"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Archer sees each other creature dying simultaneously with it")
    void simultaneousDeathsTriggerForEachOtherCreature() {
        harness.addToBattlefield(player1, new PoisonTipArcher());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CleansingNova()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Poison-Tip Archer");
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A queued death trigger resolves after Archer is removed")
    void deathTriggerSurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new PoisonTipArcher());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock(), new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Greenwood Sentinel"));
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Poison-Tip Archer"));
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Poison-Tip Archer");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Two opposing Archers dying simultaneously each see the other die")
    void opposingArchersDyingTogetherTriggerForTheirOwnOpponents() {
        harness.addToBattlefield(player1, new PoisonTipArcher());
        harness.addToBattlefield(player2, new PoisonTipArcher());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CleansingNova()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Poison-Tip Archer");
        harness.assertInGraveyard(player2, "Poison-Tip Archer");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
