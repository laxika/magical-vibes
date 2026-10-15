package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.r.RictusRobber;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OutlawMedic.class, DoomBlade.class, RictusRobber.class})
class OutlawMedicTest extends BaseCardTest {

    @Test
    @DisplayName("Lifelink gains life from combat damage")
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent medic = addCreatureReady(player1, new OutlawMedic());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(medic)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("When Outlaw Medic dies, its controller draws a card")
    void deathTriggerDrawsCard() {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addToBattlefield(player1, new OutlawMedic());

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Outlaw Medic"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Outlaw Medic");
    }

    @Test
    @DisplayName("Killing an opponent's Medic draws for that opponent after the trigger resolves")
    void deathTriggerDrawsForOpponentOnlyOnResolution() {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.setHand(player2, List.of());
        OutlawMedic drawnCard = new OutlawMedic();
        harness.setLibrary(player2, List.of(drawnCard, new OutlawMedic()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addToBattlefield(player2, new OutlawMedic());

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Outlaw Medic"));

        harness.assertInGraveyard(player2, "Outlaw Medic");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Medic gains life and draws when it dies dealing combat damage to a blocker")
    void combatDeathStillGainsLifeAndDraws() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        OutlawMedic drawnCard = new OutlawMedic();
        harness.setLibrary(player1, List.of(drawnCard, new OutlawMedic()));
        addCreatureReady(player1, new OutlawMedic());
        addCreatureReady(player2, new RictusRobber());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Outlaw Medic");
        harness.assertOnBattlefield(player2, "Rictus Robber");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
