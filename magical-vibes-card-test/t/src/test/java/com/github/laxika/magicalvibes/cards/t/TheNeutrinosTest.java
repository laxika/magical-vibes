package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MouserMarkIII;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheNeutrinos.class, MouserMarkIII.class})
class TheNeutrinosTest extends BaseCardTest {

    @Test
    void allianceBoostsWhenAnotherCreatureEnters() {
        Permanent neutrinos = addCreatureReady(player1, new TheNeutrinos());

        harness.enterBattlefieldAndReturn(player1, new MouserMarkIII());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, neutrinos)).isEqualTo(3);
    }

    @Test
    void attackAbilityReturnsChosenCreatureTappedAndAttacking() {
        Permanent neutrinos = addCreatureReady(player1, new TheNeutrinos());
        Card targetCard = new MouserMarkIII();
        targetCard.setOwnerId(player1.getId());
        Permanent target = addCreatureReady(player1, targetCard);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MouserMarkIII());

        declareAttackers(List.of(0));
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Mouser Mark III");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttackedThisTurn()).isFalse();
        assertThat(findPermanent(player1, "The Neutrinos")).isSameAs(neutrinos);
    }

    @Test
    void attackAbilityCanResolveWithoutChoosingATarget() {
        addCreatureReady(player1, new TheNeutrinos());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Neutrinos");
    }

    @Test
    void returnedCreatureIsAttackingWithoutBeingDeclaredEvenWhenItCannotAttack() {
        addCreatureReady(player1, new TheNeutrinos());
        Permanent target = addCreatureReady(player1, new MouserMarkIII());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();

            Permanent returned = findPermanent(player1, "Mouser Mark III");
            assertThat(returned.isTapped()).isTrue();
            assertThat(returned.isAttacking()).isTrue();
            assertThat(returned.getAttackTarget()).isEqualTo(player2.getId());
            assertThat(returned.isAttackedThisTurn()).isFalse();
        });
    }

    @Test
    void canReturnOwnedCreatureControlledByOpponentAndTriggersAlliance() {
        Permanent neutrinos = addCreatureReady(player1, new TheNeutrinos());
        Card ownedCard = new MouserMarkIII();
        ownedCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, ownedCard);
        gd.stolenCreatures.put(target.getId(), player1.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            resolveAllTriggers();

            harness.assertNotOnBattlefield(player2, "Mouser Mark III");
            Permanent returned = findPermanent(player1, "Mouser Mark III");
            assertThat(returned.getId()).isNotEqualTo(target.getId());
            assertThat(gqs.getEffectivePower(gd, neutrinos)).isEqualTo(3);
        });
    }

    @Test
    void allianceDoesNotTriggerForItselfOrOpponentsCreatures() {
        Permanent neutrinos = harness.enterBattlefieldAndReturn(player1, new TheNeutrinos());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, neutrinos)).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player2, new MouserMarkIII());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, neutrinos)).isEqualTo(2);
    }

    @Test
    void cannotTargetOpponentsCreatureEvenWhenYouControlIt() {
        addCreatureReady(player1, new TheNeutrinos());
        Card stolenCard = new MouserMarkIII();
        stolenCard.setOwnerId(player2.getId());
        Permanent stolen = addCreatureReady(player1, stolenCard);
        gd.stolenCreatures.put(stolen.getId(), player2.getId());

        declareAttackers(List.of(0));
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, stolen.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItselfAndReturnsAsANewCreatureWithoutAnAllianceBonus() {
        Permanent original = addCreatureReady(player1, new TheNeutrinos());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, original.getId());
            resolveAllTriggers();

            Permanent returned = findPermanent(player1, "The Neutrinos");
            assertThat(returned.getId()).isNotEqualTo(original.getId());
            assertThat(returned.isTapped()).isTrue();
            assertThat(returned.isAttacking()).isTrue();
            assertThat(returned.isAttackedThisTurn()).isFalse();
            assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        });
    }

    @Test
    void allianceBonusesAccumulateAndExpireAtEndOfTurn() {
        Permanent neutrinos = addCreatureReady(player1, new TheNeutrinos());
        harness.enterBattlefieldAndReturn(player1, new MouserMarkIII());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new MouserMarkIII());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, neutrinos)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, neutrinos)).isEqualTo(4);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, neutrinos)).isEqualTo(2);
    }
}
