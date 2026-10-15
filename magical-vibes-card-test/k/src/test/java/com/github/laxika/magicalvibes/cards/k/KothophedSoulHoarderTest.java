package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KothophedSoulHoarder.class, CruelEdict.class, GloriousAnthem.class,
        GrizzlyBears.class, Naturalize.class, WrathOfGod.class})
class KothophedSoulHoarderTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and loses 1 life when another player's creature dies")
    void drawsAndLosesLifeOnOpponentCreatureDeath() {
        harness.addToBattlefield(player1, new KothophedSoulHoarder());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Kothophed, Soul Hoarder");

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Triggers for any permanent type, not just creatures")
    void triggersOnNoncreaturePermanent() {
        harness.addToBattlefield(player1, new KothophedSoulHoarder());
        harness.addToBattlefield(player2, new GloriousAnthem());

        UUID anthemId = harness.getPermanentId(player2, "Glorious Anthem");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, anthemId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Kothophed, Soul Hoarder");

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger when a permanent its controller owns dies")
    void doesNotTriggerOnOwnPermanent() {
        harness.addToBattlefield(player1, new KothophedSoulHoarder());
        harness.addToBattlefield(player1, new GloriousAnthem());

        UUID anthemId = harness.getPermanentId(player1, "Glorious Anthem");

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, anthemId);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersForEachOpponentCreatureDyingAlongsideKothophed() {
        harness.addToBattlefield(player1, new KothophedSoulHoarder());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        harness.assertInGraveyard(player1, "Kothophed, Soul Hoarder");
        assertThat(gd.stack).hasSize(2);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        harness.assertLife(player1, lifeBefore - 2);
    }

    @Test
    void triggersForOpponentOwnedPermanentControlledByItsController() {
        harness.addToBattlefield(player1, new KothophedSoulHoarder());
        var anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        gd.stolenCreatures.put(anthem.getId(), player2.getId());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, anthem.getId());

        harness.assertInGraveyard(player2, "Glorious Anthem");
        assertThat(gd.stack).hasSize(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    void doesNotTriggerForOwnPermanentControlledByOpponent() {
        harness.addToBattlefield(player1, new KothophedSoulHoarder());
        var anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        gd.stolenCreatures.put(anthem.getId(), player1.getId());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, anthem.getId());

        harness.assertInGraveyard(player1, "Glorious Anthem");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentOwnedKothophedTriggersForItsOwnDeath() {
        var kothophed = harness.addToBattlefieldAndReturn(player2, new KothophedSoulHoarder());
        gd.stolenCreatures.put(kothophed.getId(), player1.getId());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Kothophed, Soul Hoarder");
        assertThat(gd.stack).hasSize(1);
        int handBefore = gd.playerHands.get(player2.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        harness.assertLife(player2, lifeBefore - 1);
    }
}
