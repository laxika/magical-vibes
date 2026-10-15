package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSerpentSociety.class, Shock.class, TyphoidRats.class, GrizzlyBears.class, ProdigalPyromancer.class})
class TheSerpentSocietyTest extends BaseCardTest {

    @Test
    @DisplayName("Ward lets the targeted spell resolve when its controller gets five poison counters")
    void wardCanBePaidWithPoisonCounters() {
        addCreatureReady(player1, new TheSerpentSociety());
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, findPermanent(player1, "The Serpent Society").getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(5);
        harness.assertOnBattlefield(player1, "The Serpent Society");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Declining poison-counter ward counters the targeted spell")
    void wardCountersWhenPoisonCountersAreDeclined() {
        addCreatureReady(player1, new TheSerpentSociety());
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, findPermanent(player1, "The Serpent Society").getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("A dying allied deathtouch creature makes each opponent sacrifice a nontoken creature")
    void deathtouchCreatureDeathForcesOpponentSacrifice() {
        addCreatureReady(player1, new TheSerpentSociety());
        Permanent rats = addCreatureReady(player1, new TyphoidRats());
        addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, rats.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Typhoid Rats");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A dying allied creature without deathtouch does not trigger the sacrifice ability")
    void nonDeathtouchCreatureDeathDoesNotTrigger() {
        addCreatureReady(player1, new TheSerpentSociety());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining poison ward counters an opponent's activated ability")
    void wardCountersActivatedAbilityWhenDeclined() {
        Permanent society = addCreatureReady(player1, new TheSerpentSociety());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, society.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(society.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
    }

    @Test
    @DisplayName("Accepting poison ward for an activated ability gives its controller five counters")
    void wardPaymentForActivatedAbilityAddsPoison() {
        Permanent society = addCreatureReady(player1, new TheSerpentSociety());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, society.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(5);
        assertThat(society.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's deathtouch creature dying does not trigger the sacrifice ability")
    void opposingDeathtouchCreatureDeathDoesNotTrigger() {
        addCreatureReady(player1, new TheSerpentSociety());
        Permanent rats = addCreatureReady(player2, new TyphoidRats());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, rats.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Typhoid Rats");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The Serpent Society's own death does not trigger its sacrifice ability")
    void ownDeathDoesNotTrigger() {
        Permanent society = addCreatureReady(player1, new TheSerpentSociety());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, society.getId());
        harness.castAndResolveInstant(player1, 0, society.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "The Serpent Society");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Only nontoken creatures may be chosen for the sacrifice")
    void opponentChoosesWhichNontokenCreatureToSacrifice() {
        addCreatureReady(player1, new TheSerpentSociety());
        Permanent rats = addCreatureReady(player1, new TyphoidRats());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = addCreatureReady(player2, tokenCard);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, rats.getId());
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId).contains(first.getId(), token.getId())
                .doesNotContain(second.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }
}
