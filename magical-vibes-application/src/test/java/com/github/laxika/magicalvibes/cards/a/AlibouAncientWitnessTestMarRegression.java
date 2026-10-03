package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlibouAncientWitness.class, Ornithopter.class, AccordersShield.class, GrizzlyBears.class, Forest.class, Unsummon.class})
class AlibouAncientWitnessTestMarRegression extends BaseCardTest {

    @Test
    @DisplayName("Gives other artifact creatures you control haste")
    void givesOtherArtifactCreaturesHaste() {
        Permanent alibou = addCreatureReady(player1, new AlibouAncientWitness());
        Permanent artifactCreature = addCreatureReady(player1, new Ornithopter());
        Permanent nonArtifactCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, alibou, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonArtifactCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Deals damage and scries based on tapped artifacts after an artifact creature attacks")
    void attacksTriggerDamageAndScry() {
        addCreatureReady(player1, new AlibouAncientWitness());
        addCreatureReady(player1, new Ornithopter());
        Permanent tappedArtifact = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        tappedArtifact.tap();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when only a nonartifact creature attacks")
    void doesNotTriggerForNonartifactAttacker() {
        addCreatureReady(player1, new AlibouAncientWitness());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Alibou, Ancient Witness"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotGrantHasteToOpponentsArtifactCreatures() {
        harness.addToBattlefield(player1, new AlibouAncientWitness());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    void canDamageItsControllerAndScry() {
        addCreatureReady(player1, new AlibouAncientWitness());
        addCreatureReady(player1, new Ornithopter());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerStillResolvesAfterOnlyArtifactAttackerLeaves() {
        addCreatureReady(player1, new AlibouAncientWitness());
        Permanent attacker = addCreatureReady(player1, new Ornithopter());
        harness.addToBattlefieldAndReturn(player1, new AccordersShield()).tap();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countsArtifactsTappedAfterTriggeringAndOnlyThoseControlledByYou() {
        addCreatureReady(player1, new AlibouAncientWitness());
        addCreatureReady(player1, new Ornithopter());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        harness.addToBattlefieldAndReturn(player2, new AccordersShield()).tap();
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, player2.getId());
        shield.tap();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noTappedArtifactsMeansNoDamageOrScry() {
        addCreatureReady(player1, new AlibouAncientWitness());
        Permanent attacker = addCreatureReady(player1, new Ornithopter());
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, player2.getId());
        attacker.untap();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void illegalTargetPreventsScry() {
        addCreatureReady(player1, new AlibouAncientWitness());
        addCreatureReady(player1, new Ornithopter());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
