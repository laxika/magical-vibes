package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GatheringThrong;
import com.github.laxika.magicalvibes.cards.d.DapperShieldmate;
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

@CardUsed({CivilServant.class, GatheringThrong.class, DapperShieldmate.class})
class CivilServantTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping another Citizen boosts Civil Servant and grants lifelink")
    void tappingAnotherCitizenBoostsAndGrantsLifelink() {
        Permanent servant = addCreatureReady(player1, new CivilServant());
        Permanent citizen = addCreatureReady(player1, new GatheringThrong());

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
        Permanent citizen = addCreatureReady(player1, new GatheringThrong());

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
        Permanent soldier = addCreatureReady(player1, new DapperShieldmate());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(soldier.isTapped()).isFalse();
        assertThat(servant.getEffectivePower()).isEqualTo(2);
        assertThat(servant.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, servant, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The Civil Servant boost and lifelink wear off at end of turn")
    void boostAndLifelinkWearOffAtEndOfTurn() {
        Permanent servant = addCreatureReady(player1, new CivilServant());
        Permanent citizen = addCreatureReady(player1, new GatheringThrong());

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

    @Test
    void canTapASummoningSickCitizen() {
        Permanent servant = addCreatureReady(player1, new CivilServant());
        Permanent citizen = harness.addToBattlefieldAndReturn(player1, new GatheringThrong());
        citizen.setSummoningSick(true);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(citizen.isTapped()).isTrue();
        assertThat(servant.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, servant, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void cannotTapAnOpponentsCitizen() {
        Permanent servant = addCreatureReady(player1, new CivilServant());
        Permanent citizen = addCreatureReady(player2, new GatheringThrong());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(citizen.isTapped()).isFalse();
        assertThat(servant.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, servant, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void cannotPayWithAnAlreadyTappedCitizen() {
        Permanent servant = addCreatureReady(player1, new CivilServant());
        Permanent citizen = addCreatureReady(player1, new GatheringThrong());
        citizen.tap();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(citizen.isTapped()).isTrue();
        assertThat(servant.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, servant, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void choosesExactlyOneOfMultipleEligibleCitizens() {
        Permanent servant = addCreatureReady(player1, new CivilServant());
        Permanent first = addCreatureReady(player1, new GatheringThrong());
        Permanent second = addCreatureReady(player1, new GatheringThrong());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
        assertThat(servant.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, servant, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void cannotTapItselfEvenIfUntappedBeforeTheTriggerResolves() {
        Permanent servant = addCreatureReady(player1, new CivilServant());

        declareAttackers(List.of(0));
        servant.untap();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(servant.isTapped()).isFalse();
        assertThat(servant.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, servant, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void lifelinkGainsLifeFromBoostedCombatDamage() {
        addCreatureReady(player1, new CivilServant());
        addCreatureReady(player1, new GatheringThrong());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }
}
