package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MothriderSamurai;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkkiRonin.class, MothriderSamurai.class, ElvishWarrior.class, GrizzlyBears.class, Forest.class,
        WitnessProtection.class})
class AkkiRoninTest extends BaseCardTest {

    @Test
    @DisplayName("Akki Ronin itself attacking alone may discard and draw")
    void selfAttackingAloneMayDiscardAndDraw() {
        addCreatureReady(player1, new AkkiRonin());
        Forest discarded = new Forest();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("With no card to discard, the trigger cannot draw")
    void emptyHandDoesNotDraw() {
        addCreatureReady(player1, new AkkiRonin());
        Forest topCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opposing Samurai attacking alone does not trigger your Akki Ronin")
    void opposingSamuraiDoesNotTrigger() {
        addCreatureReady(player1, new AkkiRonin()).setTapped(true);
        addCreatureReady(player2, new AkkiRonin());
        Forest retained = new Forest();
        harness.setHand(player1, List.of(retained));
        harness.setHand(player2, List.of());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, false);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
    }

    @Test
    @DisplayName("A Samurai changed into a Citizen does not trigger Akki Ronin")
    void replacedCreatureTypesDoNotTrigger() {
        addCreatureReady(player1, new AkkiRonin());
        Permanent attacker = addCreatureReady(player1, new AkkiRonin());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WitnessProtection());
        aura.setAttachedTo(attacker.getId());
        Forest retained = new Forest();
        harness.setHand(player1, List.of(retained));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
    }

    @Test
    @DisplayName("A Samurai attacking alone may discard and draw")
    void samuraiAttackingAloneMayDiscardAndDraw() {
        addCreatureReady(player1, new AkkiRonin());
        addCreatureReady(player1, new MothriderSamurai());
        GrizzlyBears discarded = new GrizzlyBears();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("A Warrior attacking alone may discard and draw")
    void warriorAttackingAloneMayDiscardAndDraw() {
        addCreatureReady(player1, new AkkiRonin());
        addCreatureReady(player1, new ElvishWarrior());
        GrizzlyBears discarded = new GrizzlyBears();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Declining the attack trigger does not discard or draw")
    void decliningAttackTriggerDoesNothing() {
        addCreatureReady(player1, new AkkiRonin());
        addCreatureReady(player1, new MothriderSamurai());
        GrizzlyBears retained = new GrizzlyBears();
        harness.setHand(player1, List.of(retained));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A non-Samurai, non-Warrior attacking alone does not trigger")
    void otherCreatureAttackingAloneDoesNotTrigger() {
        addCreatureReady(player1, new AkkiRonin());
        addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears retained = new GrizzlyBears();
        harness.setHand(player1, List.of(retained));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
    }

    @Test
    @DisplayName("A Samurai attacking with another creature does not trigger")
    void samuraiAttackingWithAnotherCreatureDoesNotTrigger() {
        addCreatureReady(player1, new AkkiRonin());
        addCreatureReady(player1, new MothriderSamurai());
        addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears retained = new GrizzlyBears();
        harness.setHand(player1, List.of(retained));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
    }
}
