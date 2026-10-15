package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuinousWaterbending.class, GrizzlyBears.class, HillGiant.class, Terror.class})
class RuinousWaterbendingTest extends BaseCardTest {

    @Test
    @DisplayName("Gives all creatures -2/-2 without waterbending")
    void weakensAllCreaturesWithoutWaterbending() {
        Permanent survivingCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RuinousWaterbending()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, survivingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, survivingCreature)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Waterbending gains life for each creature that dies this turn")
    void waterbendingGainsLifeForEachCreatureThatDies() {
        List<Permanent> paymentCreatures = List.of(
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        harness.setHand(player1, List.of(new RuinousWaterbending()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null,
                paymentCreatures.stream().map(Permanent::getId).toList(), List.of(), false,
                null, null, List.of(), List.of(), null, null, true);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(paymentCreatures).allMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Waterbend can be paid entirely with mana and counts opposing deaths")
    void manaOnlyWaterbendCountsOpposingDeaths() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RuinousWaterbending()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        castWithWaterbendPaid();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Later creatures are not weakened but their deaths still trigger life gain")
    void laterCreatureIsNotWeakenedButItsDeathTriggers() {
        harness.setHand(player1, List.of(new RuinousWaterbending(), new Terror()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        castWithWaterbendPaid();
        harness.passBothPriorities();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, laterCreature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Both the weakening and the delayed life gain expire at turn cleanup")
    void weakeningAndDelayedTriggerExpireAtCleanup() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new RuinousWaterbending(), new Terror()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        castWithWaterbendPaid();
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(1);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, survivor.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each paid casting creates its own delayed life gain ability")
    void multiplePaidCastingsTriggerIndependently() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new RuinousWaterbending(), new RuinousWaterbending()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        castWithWaterbendPaid();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        castWithWaterbendPaid();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertLife(player1, 22);
    }

    private void castWithWaterbendPaid() {
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null,
                List.of(), List.of(), false, null, null, List.of(), List.of(), null, null, true);
    }
}
