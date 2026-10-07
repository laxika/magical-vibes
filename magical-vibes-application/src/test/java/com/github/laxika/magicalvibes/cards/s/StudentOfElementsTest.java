package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ConsumingVortex;
import com.github.laxika.magicalvibes.cards.c.CorruptionOfTowashi;
import com.github.laxika.magicalvibes.cards.l.LiftedByClouds;
import com.github.laxika.magicalvibes.cards.t.TobitaMasterOfWinds;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StudentOfElements.class, TobitaMasterOfWinds.class, LiftedByClouds.class,
        WanderingOnes.class, SoratamiCloudskater.class, ConsumingVortex.class})
class StudentOfElementsTest extends BaseCardTest {

    private Permanent addStudent() {
        return addCreatureReady(player1, new StudentOfElements());
    }

    @Test
    @DisplayName("Stays unflipped while it doesn't have flying")
    void staysUnflippedWithoutFlying() {
        Permanent student = addStudent();

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(student.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not flip when another creature has flying")
    void doesNotFlipForAnotherCreatureWithFlying() {
        Permanent student = addStudent();
        addCreatureReady(player1, new SoratamiCloudskater());

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(student.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Flips into Tobita when it gains flying")
    void flipsWhenItGainsFlying() {
        Permanent student = addStudent();
        harness.setHand(player1, List.of(new LiftedByClouds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, student.getId());
        harness.passBothPriorities(); // trigger resolves -> flipped

        assertThat(student.isTransformed()).isTrue();
        assertThat(student.getCard()).isInstanceOf(TobitaMasterOfWinds.class);
    }

    @Test
    @DisplayName("Does not retrigger while the flip trigger is on the stack")
    void doesNotRetriggerWhileTriggerIsOnStack() {
        Permanent student = addStudent();
        harness.setHand(player1, List.of(new LiftedByClouds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, student.getId());

        assertThat(gd.stack).hasSize(1);
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(student.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Once flipped, Tobita gives every creature you control flying")
    void flippedGrantsFlyingToOwnCreatures() {
        Permanent student = addStudent();
        student.setTransformed(true);
        student.setCard(student.getOriginalCard().getBackFaceCard());
        Permanent ownCreature = addCreatureReady(player1, new WanderingOnes());
        Permanent opponentCreature = addCreatureReady(player2, new WanderingOnes());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, student, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flipping does not trigger abilities that watch a permanent transform")
    @CardUsed({CorruptionOfTowashi.class})
    void flippingDoesNotCountAsTransforming() {
        harness.castFromHand(player1, new CorruptionOfTowashi(), "{4}{U}");
        resolveAllTriggers();
        Permanent student = addStudent();
        harness.setHand(player1, List.of(new LiftedByClouds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, student.getId());
        harness.passBothPriorities();

        assertThat(student.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The flip trigger cannot affect a Student that has left the battlefield")
    void doesNotFlipAfterLeavingBattlefield() {
        Permanent student = addStudent();
        harness.setHand(player1, List.of(new LiftedByClouds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, student.getId());
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new ConsumingVortex()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, student.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Student of Elements");
        harness.assertNotOnBattlefield(player1, "Tobita, Master of Winds");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tobita grants flying to creatures that arrive later and does not retrigger")
    void grantsFlyingToLaterCreaturesWithoutRetriggering() {
        Permanent student = addStudent();
        harness.setHand(player1, List.of(new LiftedByClouds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, student.getId());
        harness.passBothPriorities();

        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new WanderingOnes());
        harness.runStateBasedActions();

        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.FLYING)).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
