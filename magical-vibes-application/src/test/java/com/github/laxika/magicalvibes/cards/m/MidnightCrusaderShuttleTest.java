package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AdiposeOffspring;
import com.github.laxika.magicalvibes.cards.t.TheValeyard;
import com.github.laxika.magicalvibes.cards.y.YasharnImplacableEarth;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MidnightCrusaderShuttle.class, AdiposeOffspring.class, TheValeyard.class,
        YasharnImplacableEarth.class})
class MidnightCrusaderShuttleTest extends BaseCardTest {

    @Test
    void defenderSacrificesCreature() {
        addReadyShuttle();
        addCreatureReady(player1, new AdiposeOffspring());
        Permanent defendingCreature = addCreatureReady(player2, new AdiposeOffspring());

        crewShuttle();
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(defendingCreature);
    }

    @Test
    void defenderDeclinesAndAttackerTakesCreatureTappedAndAttacking() {
        Permanent shuttle = addReadyShuttle();
        addCreatureReady(player1, new AdiposeOffspring());
        Permanent defendingCreature = addCreatureReady(player2, new AdiposeOffspring());
        Permanent otherDefendingCreature = addCreatureReady(player2, new AdiposeOffspring());

        crewShuttle();
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handlePermanentChosen(player1, defendingCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(defendingCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherDefendingCreature);
        assertThat(defendingCreature.isTapped()).isTrue();
        assertThat(defendingCreature.isAttacking()).isTrue();
        assertThat(defendingCreature.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(shuttle.isAttacking()).isTrue();
    }

    @Test
    void defenderStillFacesChoiceWithoutCreatures() {
        addReadyShuttle();
        addCreatureReady(player1, new AdiposeOffspring());
        crewShuttle();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void valeyardMakesDefenderFaceChoiceAgainAfterSacrificing() {
        addReadyShuttle();
        addCreatureReady(player1, new AdiposeOffspring());
        addCreatureReady(player2, new AdiposeOffspring());
        crewShuttle();
        harness.addToBattlefield(player1, new TheValeyard());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void soleTappedSummoningSickCreatureCanBeTakenAndAttack() {
        addReadyShuttle();
        addCreatureReady(player1, new AdiposeOffspring());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AdiposeOffspring());
        creature.setSummoningSick(true);
        creature.setTapped(true);
        crewShuttle();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    void yasharnDoesNotPreventSacrificeDuringResolution() {
        addReadyShuttle();
        addCreatureReady(player1, new AdiposeOffspring());
        addCreatureReady(player2, new AdiposeOffspring());
        crewShuttle();
        harness.addToBattlefield(player1, new YasharnImplacableEarth());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Adipose Offspring");
    }

    private Permanent addReadyShuttle() {
        return addCreatureReady(player1, new MidnightCrusaderShuttle());
    }

    private void crewShuttle() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
