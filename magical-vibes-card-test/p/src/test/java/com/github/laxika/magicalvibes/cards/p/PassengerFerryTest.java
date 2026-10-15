package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MercilessEnforcers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PassengerFerry.class, MercilessEnforcers.class})
class PassengerFerryTest extends BaseCardTest {

    @Test
    void crewAnimatesFerryAndTapsCrew() {
        Permanent ferry = addFerryReady();
        Permanent crew = addCreatureReady(player1, new MercilessEnforcers());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ferry)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void payingBlueMakesAnotherAttackingCreatureUnblockable() {
        Permanent ferry = addFerryReady();
        addCreatureReady(player1, new MercilessEnforcers());
        Permanent attacker = addCreatureReady(player1, new MercilessEnforcers());
        attacker.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        attacker.untap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isTrue();
        assertThat(ferry.isCantBeBlocked()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void decliningBlueDoesNotMakeAttackerUnblockable() {
        addFerryReady();
        addCreatureReady(player1, new MercilessEnforcers());
        Permanent attacker = addCreatureReady(player1, new MercilessEnforcers());
        attacker.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        attacker.untap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(attacker.isCantBeBlocked()).isFalse();
    }

    @Test
    void attackTriggerCannotTargetTheFerryItself() {
        Permanent ferry = addFerryReady();
        addCreatureReady(player1, new MercilessEnforcers());
        Permanent attacker = addCreatureReady(player1, new MercilessEnforcers());
        attacker.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        attacker.untap();

        harness.addMana(player1, ManaColor.BLUE, 1);
        declareAttackers(player1, List.of(0, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ferry.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        assertThat(attacker.isCantBeBlocked()).isTrue();
    }

    @Test
    void attackTriggerOffersPaymentBeforeChoosingAnyTarget() {
        addFerryReady();
        addCreatureReady(player1, new MercilessEnforcers());
        Permanent attacker = addCreatureReady(player1, new MercilessEnforcers());
        attacker.tap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        attacker.untap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(attacker.isCantBeBlocked()).isFalse();
    }

    @Test
    void attackingAloneStillAllowsPayment() {
        addFerryReady();
        addCreatureReady(player1, new MercilessEnforcers());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void reflexiveTriggerCannotTargetANonattackingCreature() {
        addFerryReady();
        Permanent crew = addCreatureReady(player1, new MercilessEnforcers());
        Permanent attacker = addCreatureReady(player1, new MercilessEnforcers());
        attacker.tap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        attacker.untap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, crew.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        assertThat(attacker.isCantBeBlocked()).isTrue();
    }

    @Test
    void summoningSickCreatureCanPayCrewCost() {
        Permanent ferry = addFerryReady();
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new MercilessEnforcers());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, ferry)).isTrue();
    }

    private Permanent addFerryReady() {
        return addCreatureReady(player1, new PassengerFerry());
    }
}
