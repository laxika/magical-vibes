package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JeskaiMonument;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FelotharDawnOfTheAbzan.class, Forest.class, GrizzlyBears.class})
class FelotharDawnOfTheAbzanTest extends BaseCardTest {

    @Test
    @DisplayName("ETB sacrifice puts a +1/+1 counter on each controlled creature")
    void etbSacrificePutsCountersOnControlledCreatures() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFelothar();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        Permanent felothar = findPermanent(player1, "Felothar, Dawn of the Abzan");
        assertThat(felothar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
    }

    @Test
    @DisplayName("Declining the ETB sacrifice does nothing")
    void decliningEtbSacrificeDoesNothing() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent felothar = castFelothar();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(felothar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Attacking and sacrificing a permanent puts counters on controlled creatures")
    void attackingSacrificePutsCountersOnControlledCreatures() {
        Permanent felothar = addCreatureReady(player1, new FelotharDawnOfTheAbzan());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent survivor = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(felothar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The sacrifice choice contains nonland permanents but not lands")
    void sacrificeChoiceExcludesLands() {
        Permanent felothar = addCreatureReady(player1, new FelotharDawnOfTheAbzan());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(felothar.getId());
        assertThat(choice.validIds()).doesNotContain(land.getId());
    }

    @Test
    @CardUsed(JeskaiMonument.class)
    @DisplayName("A noncreature artifact can be sacrificed for counters")
    void sacrificingArtifactPutsCountersOnFelothar() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new JeskaiMonument());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new JeskaiMonument());
        Permanent felothar = castFelothar();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(artifact.getId());
        assertThat(choice.validIds()).doesNotContain(opponentArtifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jeskai Monument");
        assertThat(felothar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentArtifact);
    }

    @Test
    @DisplayName("Felothar may sacrifice itself and still put counters on surviving creatures")
    void sacrificingFelotharStillPutsCountersOnSurvivors() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent felothar = castFelothar();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, felothar.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Felothar, Dawn of the Abzan");
        harness.assertNotOnBattlefield(player1, "Felothar, Dawn of the Abzan");
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters wait for the reflexive trigger and include creatures entering before it resolves")
    void countersUseBattlefieldWhenReflexiveTriggerResolves() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent felothar = castFelothar();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(felothar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(felothar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining the attack sacrifice preserves the creature and adds no counters")
    void decliningAttackSacrificeDoesNothing() {
        Permanent felothar = addCreatureReady(player1, new FelotharDawnOfTheAbzan());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(survivor);
        assertThat(felothar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castFelothar() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new FelotharDawnOfTheAbzan(), "{W}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Felothar, Dawn of the Abzan");
    }
}
