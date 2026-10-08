package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mirrorweave;
import com.github.laxika.magicalvibes.cards.s.StormfrontRiders;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WizardMentor.class, CoralMerfolk.class, Island.class,
        Mirrorweave.class, StormfrontRiders.class})
class WizardMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself and a creature you control to their owners' hands")
    void returnsSelfAndControlledCreature() {
        Permanent mentor = addCreatureReady(player1, new WizardMentor());
        Permanent merfolk = addCreatureReady(player1, new CoralMerfolk());

        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Wizard Mentor");
        harness.assertInHand(player1, "Coral Merfolk");
        harness.assertNotOnBattlefield(player1, "Wizard Mentor");
        harness.assertNotOnBattlefield(player1, "Coral Merfolk");
        assertThat(mentor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Returns a creature you control to its owner's hand")
    void returnsControlledCreatureToItsOwnersHand() {
        addCreatureReady(player1, new WizardMentor());
        CoralMerfolk merfolkCard = new CoralMerfolk();
        merfolkCard.setOwnerId(player2.getId());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, merfolkCard);
        gd.stolenCreatures.put(merfolk.getId(), player2.getId());
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player1.getId(), merfolk,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));

        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Wizard Mentor");
        harness.assertInHand(player2, "Coral Merfolk");
        harness.assertNotOnBattlefield(player1, "Coral Merfolk");
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        Permanent mentor = addCreatureReady(player1, new WizardMentor());

        harness.activateAbility(player1, 0, null, mentor.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Wizard Mentor");
        harness.assertNotOnBattlefield(player1, "Wizard Mentor");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        addCreatureReady(player1, new WizardMentor());
        Permanent merfolk = addCreatureReady(player2, new CoralMerfolk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, merfolk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent you control")
    void cannotTargetControlledNoncreature() {
        addCreatureReady(player1, new WizardMentor());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not return itself when its only target leaves before resolution")
    void doesNotReturnSelfWhenTargetLeaves() {
        Permanent mentor = addCreatureReady(player1, new WizardMentor());
        addCreatureReady(player1, new WizardMentor());
        Permanent merfolk = addCreatureReady(player1, new CoralMerfolk());

        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.activateAbility(player1, 1, null, merfolk.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(mentor);
        assertThat(mentor.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Wizard Mentor")).hasSize(1);
        harness.assertInHand(player1, "Coral Merfolk");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Still returns the target when the source leaves before resolution")
    void returnsTargetWhenSourceLeaves() {
        Permanent mentor = addCreatureReady(player1, new WizardMentor());
        addCreatureReady(player1, new WizardMentor());
        Permanent merfolk = addCreatureReady(player1, new CoralMerfolk());

        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.activateAbility(player1, 1, null, mentor.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Wizard Mentor")).hasSize(2);
        harness.assertInHand(player1, "Coral Merfolk");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new WizardMentor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mentor.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Wizard Mentor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        Permanent mentor = addCreatureReady(player1, new WizardMentor());
        mentor.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mentor.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Wizard Mentor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both creatures return simultaneously for a source that gains a return trigger")
    void simultaneousReturnPreservesSourceReturnTriggers() {
        addCreatureReady(player1, new WizardMentor());
        Permanent merfolk = addCreatureReady(player1, new CoralMerfolk());
        Permanent riders = addCreatureReady(player2, new StormfrontRiders());
        harness.setHand(player1, List.of(new Mirrorweave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.castInstant(player1, 0, riders.getId());
        harness.passBothPriorities();

        resolveAllTriggers();

        harness.assertInHand(player1, "Wizard Mentor");
        harness.assertInHand(player1, "Coral Merfolk");
        assertThat(findPermanents(player1, "Soldier")).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }
}
