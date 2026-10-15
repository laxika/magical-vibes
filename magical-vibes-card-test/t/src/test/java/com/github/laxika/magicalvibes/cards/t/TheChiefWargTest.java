package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheChiefWarg.class, AirElemental.class, GrizzlyBears.class, Unsummon.class})
class TheChiefWargTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and loses 1 life when attacking while controlling a creature with power 4 or greater")
    void drawsAndLosesLifeForFerociousAttack() {
        addCreatureReady(player1, new TheChiefWarg());
        addCreatureReady(player1, new AirElemental());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int lifeBeforeAttack = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBeforeAttack - 1);
    }

    @Test
    @DisplayName("Does not trigger without a creature with power 4 or greater")
    void doesNotTriggerWithoutQualifyingCreature() {
        addCreatureReady(player1, new TheChiefWarg());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBeforeAttack = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBeforeAttack);
    }

    @Test
    @DisplayName("Does not trigger from an opponent's qualifying creature")
    void doesNotTriggerFromOpponentsCreature() {
        addCreatureReady(player1, new TheChiefWarg());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new AirElemental());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBeforeAttack = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBeforeAttack);
    }

    @Test
    @DisplayName("Ferocious remains satisfied after the qualifying creature is returned to hand")
    void resolvesAfterQualifyingCreatureLeaves() {
        addCreatureReady(player1, new TheChiefWarg());
        var qualifyingCreature = addCreatureReady(player1, new AirElemental());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int lifeBeforeAttack = gd.playerLifeTotals.get(player1.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);
            harness.castInstant(player1, 0, qualifyingCreature.getId());
            harness.passBothPriorities();
            harness.assertNotOnBattlefield(player1, "Air Elemental");
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, lifeBeforeAttack - 1);
    }

    @Test
    @DisplayName("Triggers once for multiple attackers even when The Chief Warg stays back")
    void triggersOnceWithoutChiefAttacking() {
        harness.addToBattlefield(player1, new TheChiefWarg());
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int lifeBeforeAttack = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, lifeBeforeAttack - 1);
    }

    @Test
    @DisplayName("Declaring no attackers does not trigger ferocious")
    void doesNotTriggerWithoutAttacking() {
        addCreatureReady(player1, new TheChiefWarg());
        addCreatureReady(player1, new AirElemental());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBeforeAttack = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, lifeBeforeAttack);
    }

    @Test
    @DisplayName("An opponent attacking does not trigger The Chief Warg")
    void doesNotTriggerWhenOpponentAttacks() {
        harness.addToBattlefield(player1, new TheChiefWarg());
        harness.addToBattlefield(player1, new AirElemental());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBeforeAttack = gd.playerLifeTotals.get(player1.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            assertThat(gd.stack).isEmpty();
        });

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, lifeBeforeAttack);
    }

    @Test
    @DisplayName("The attack trigger resolves after The Chief Warg leaves the battlefield")
    void resolvesAfterChiefLeaves() {
        var chief = addCreatureReady(player1, new TheChiefWarg());
        addCreatureReady(player1, new AirElemental());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int lifeBeforeAttack = gd.playerLifeTotals.get(player1.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            assertThat(gd.stack).hasSize(1);
            harness.castInstant(player1, 0, chief.getId());
            harness.passBothPriorities();
            harness.assertNotOnBattlefield(player1, "The Chief Warg");
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, lifeBeforeAttack - 1);
    }
}
