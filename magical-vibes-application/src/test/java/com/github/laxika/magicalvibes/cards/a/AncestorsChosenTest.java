package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ErhnamDjinn;
import com.github.laxika.magicalvibes.cards.b.BenevolentBodyguard;
import com.github.laxika.magicalvibes.cards.c.Cagemail;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({
        AncestorsChosen.class,
        AvenWarcraft.class,
        BenevolentBodyguard.class,
        Cagemail.class,
        ErhnamDjinn.class,
        SuntailHawk.class
})
class AncestorsChosenTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains one life for each card in its controller's graveyard")
    void etbGainsLifeForEachCardInControllerGraveyard() {
        harness.setLife(player1, 10);
        harness.setGraveyard(player1, List.of(
                new BenevolentBodyguard(), new Cagemail(), new SuntailHawk()));
        harness.setGraveyard(player2, List.of(new BenevolentBodyguard(), new Cagemail()));

        castAncestorsChosen();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB gains no life when its controller's graveyard is empty")
    void etbGainsNoLifeWithEmptyControllerGraveyard() {
        harness.setLife(player1, 10);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new BenevolentBodyguard(), new Cagemail()));

        castAncestorsChosen();

        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("ETB counts cards added before its trigger resolves")
    void etbCountsCardsAtResolution() {
        harness.setLife(player1, 10);
        harness.setGraveyard(player1, List.of(new BenevolentBodyguard()));

        harness.castFromHand(player1, new AncestorsChosen(), "{5}{W}{W}");
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new BenevolentBodyguard(), new Cagemail()));
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("First strike deals combat damage before a non-first-strike creature")
    void firstStrikeDealsCombatDamageBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new AncestorsChosen());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new ErhnamDjinn());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.castFromHand(player1, new AvenWarcraft(), "{2}{W}");
        harness.passBothPriorities();
        resolveCombat();

        harness.assertOnBattlefield(player1, "Ancestor's Chosen");
        harness.assertInGraveyard(player2, "Erhnam Djinn");
    }

    private void castAncestorsChosen() {
        harness.castFromHand(player1, new AncestorsChosen(), "{5}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
