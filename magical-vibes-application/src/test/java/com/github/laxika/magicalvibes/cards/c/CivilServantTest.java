package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GallantCitizen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CivilServant.class, GallantCitizen.class, GrizzlyBears.class})
class CivilServantTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping another Citizen boosts Civil Servant and grants lifelink")
    void tappingAnotherCitizenBoostsAndGrantsLifelink() {
        Permanent servant = addCreatureReady(player1, new CivilServant());
        Permanent citizen = addCreatureReady(player1, new GallantCitizen());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(citizen.isTapped()).isTrue();
        assertThat(servant.getEffectivePower()).isEqualTo(3);
        assertThat(servant.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, servant, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Declining to tap a Citizen leaves Civil Servant unchanged")
    void decliningTapLeavesServantUnchanged() {
        Permanent servant = addCreatureReady(player1, new CivilServant());
        Permanent citizen = addCreatureReady(player1, new GallantCitizen());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(citizen.isTapped()).isFalse();
        assertThat(servant.getEffectivePower()).isEqualTo(2);
        assertThat(servant.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, servant, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A non-Citizen cannot be tapped for Civil Servant")
    void nonCitizenCannotBeTapped() {
        Permanent servant = addCreatureReady(player1, new CivilServant());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bear.isTapped()).isFalse();
        assertThat(servant.getEffectivePower()).isEqualTo(2);
        assertThat(servant.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, servant, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The Civil Servant boost and lifelink wear off at end of turn")
    void boostAndLifelinkWearOffAtEndOfTurn() {
        Permanent servant = addCreatureReady(player1, new CivilServant());
        Permanent citizen = addCreatureReady(player1, new GallantCitizen());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(citizen.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(servant.getEffectivePower()).isEqualTo(2);
        assertThat(servant.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, servant, Keyword.LIFELINK)).isFalse();
    }
}
