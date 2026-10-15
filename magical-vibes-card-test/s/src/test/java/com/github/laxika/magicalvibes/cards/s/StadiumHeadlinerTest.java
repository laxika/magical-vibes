package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StadiumHeadliner.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class StadiumHeadlinerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking red Warrior token")
    void attackingCreatesTappedAndAttackingWarriorToken() {
        addCreatureReady(player1, new StadiumHeadliner());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().isTapped()).isTrue();
        assertThat(tokens.getFirst().isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The mobilized token is sacrificed at the beginning of the next end step")
    void mobilizedTokenIsSacrificedAtNextEndStep() {
        addCreatureReady(player1, new StadiumHeadliner());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing deals damage equal to the creatures still controlled")
    void sacrificeAbilityDealsDamageEqualToCreatureCount() {
        addCreatureReady(player1, new StadiumHeadliner());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new AirElemental());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Stadium Headliner");
    }

    @Test
    @DisplayName("The sacrifice ability cannot target a noncreature permanent")
    void sacrificeAbilityCannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new StadiumHeadliner());
        harness.addToBattlefield(player2, new Forest());
        Permanent target = findPermanent(player2, "Forest");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("A summoning-sick Headliner can sacrifice itself and deals zero with no creatures remaining")
    void summoningSickHeadlinerCanActivateForZeroDamage() {
        harness.addToBattlefield(player1, new StadiumHeadliner());
        Permanent target = addCreatureReady(player2, new StadiumHeadliner());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Stadium Headliner");
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Stadium Headliner");
    }

    @Test
    @DisplayName("Damage counts creatures at resolution after another Headliner is sacrificed in response")
    void damageCountDecreasesBeforeResolution() {
        addCreatureReady(player1, new StadiumHeadliner());
        addCreatureReady(player1, new StadiumHeadliner());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(countPermanents(player1, "Stadium Headliner")).isZero();
    }

    @Test
    @DisplayName("Mobilize resolves after Headliner is sacrificed while its attack trigger is pending")
    void mobilizeResolvesWithoutItsSource() {
        addCreatureReady(player1, new StadiumHeadliner());
        Permanent target = addCreatureReady(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.activateAbility(player1, 0, null, target.getId());
            resolveAllTriggers();

            assertThat(target.getMarkedDamage()).isZero();
            Permanent token = findPermanent(player1, "Warrior");
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior")).isEmpty();
        harness.assertInGraveyard(player1, "Stadium Headliner");
    }

    @Test
    @DisplayName("The mobilized Warrior counts toward Headliner's damage")
    void mobilizedTokenCountsTowardDamage() {
        addCreatureReady(player1, new StadiumHeadliner());
        Permanent target = addCreatureReady(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.activateAbility(player1, 0, null, target.getId());
            resolveAllTriggers();

            assertThat(target.getMarkedDamage()).isEqualTo(1);
            assertThat(countPermanents(player1, "Warrior")).isEqualTo(1);
        });
    }
}
