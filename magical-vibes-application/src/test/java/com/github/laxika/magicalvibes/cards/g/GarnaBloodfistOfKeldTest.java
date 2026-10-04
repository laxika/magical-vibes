package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarnaBloodfistOfKeld.class, GrizzlyBears.class, Shock.class, Forest.class,
        LightningStrike.class, Pyroclasm.class})
class GarnaBloodfistOfKeldTest extends BaseCardTest {

    @Test
    @DisplayName("When an attacking ally dies, Garna draws a card")
    void attackingAllyDeathDrawsCard() {
        harness.addToBattlefield(player1, new GarnaBloodfistOfKeld());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));

        killWithShock(attacker);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("When a nonattacking ally dies, Garna deals 1 damage to each opponent")
    void nonattackingAllyDeathDamagesOpponents() {
        harness.addToBattlefield(player1, new GarnaBloodfistOfKeld());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());

        killWithShock(ally);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void killWithShock(Permanent target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();
    }

    @Test
    void ownDeathDoesNotTrigger() {
        Permanent garna = addCreatureReady(player1, new GarnaBloodfistOfKeld());
        garna.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, garna.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Garna, Bloodfist of Keld");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void opposingAttackingCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new GarnaBloodfistOfKeld());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        opponent.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));

        killWithShock(opponent);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void blockingAllyDeathDealsDamageInsteadOfDrawing() {
        harness.addToBattlefield(player1, new GarnaBloodfistOfKeld());
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        blocker.setBlocking(true);
        harness.setLibrary(player1, List.of(new Forest()));

        killWithShock(blocker);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void simultaneousDeathsChooseOutcomeSeparatelyEvenWhenGarnaDies() {
        Permanent garna = addCreatureReady(player1, new GarnaBloodfistOfKeld());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, garna.getId());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Garna, Bloodfist of Keld");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
