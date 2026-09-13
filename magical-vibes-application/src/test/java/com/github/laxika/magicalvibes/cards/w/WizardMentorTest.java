package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.i.Island;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WizardMentor.class, CoralMerfolk.class, Island.class})
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
        addCreatureReady(player1, new WizardMentor());
        Permanent mentor = findPermanent(player1, "Wizard Mentor");

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
}
