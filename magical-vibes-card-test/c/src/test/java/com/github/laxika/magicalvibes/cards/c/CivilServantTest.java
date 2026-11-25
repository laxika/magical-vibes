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
        assertThat(gqs.getEffectivePower(gd, servant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, servant)).isEqualTo(3);
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
        assertThat(gqs.getEffectivePower(gd, servant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, servant)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, servant, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A non-Citizen cannot be tapped for Civil Servant: nothing is asked")
    void nonCitizenCannotBeTapped() {
        Permanent servant = addCreatureReady(player1, new CivilServant());
        Permanent soldier = addCreatureReady(player1, new DapperShieldmate());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        assertThat(soldier.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, servant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, servant)).isEqualTo(3);
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

        assertThat(gqs.getEffectivePower(gd, servant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, servant)).isEqualTo(3);
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
        assertThat(gqs.getEffectivePower(gd, servant)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, servant, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Citizen cannot be tapped: nothing is asked")
    void cannotTapAnOpponentsCitizen() {
        Permanent servant = addCreatureReady(player1, new CivilServant());
        Permanent citizen = addCreatureReady(player2, new GatheringThrong());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        assertThat(citizen.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, servant)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, servant, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("An already tapped Citizen cannot pay: nothing is asked")
    void cannotPayWithAnAlreadyTappedCitizen() {
        Permanent servant = addCreatureReady(player1, new CivilServant());
        Permanent citizen = addCreatureReady(player1, new GatheringThrong());
        citizen.tap();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        assertThat(citizen.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, servant)).isEqualTo(2);
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
        assertThat(gqs.getEffectivePower(gd, servant)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, servant, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Civil Servant cannot tap itself even if untapped before the trigger resolves: nothing is asked")
    void cannotTapItselfEvenIfUntappedBeforeTheTriggerResolves() {
        Permanent servant = addCreatureReady(player1, new CivilServant());

        declareAttackers(List.of(0));
        servant.untap();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        assertThat(servant.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, servant)).isEqualTo(2);
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
