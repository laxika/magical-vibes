package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakingCanopy.class, AirElemental.class, GrizzlyBears.class, JaceBeleren.class})
class RakingCanopyTest extends BaseCardTest {

    /** Puts Raking Canopy on player1's battlefield and a ready attacker on player2's. */
    private Permanent setUpAttack(Card card) {
        harness.addToBattlefield(player1, new RakingCanopy());
        return addCreatureReady(player2, card);
    }

    @Test
    @DisplayName("A flyer attacking the controller triggers Raking Canopy against that attacker")
    void flyerTriggersAbility() {
        Permanent attacker = setUpAttack(new AirElemental());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Resolving the trigger deals 4 damage to the attacking flyer")
    void dealsFourDamageToFlyer() {
        Permanent attacker = setUpAttack(new AirElemental());

        declareAttackers(player2, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("A non-flying attacker does not trigger Raking Canopy")
    void nonFlyerDoesNotTrigger() {
        Permanent attacker = setUpAttack(new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A flyer attacking your planeswalker does not trigger Raking Canopy")
    void attackingPlaneswalkerDoesNotTrigger() {
        Permanent attacker = setUpAttack(new AirElemental());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Each attacking flyer creates its own trigger, while a ground attacker does not")
    void triggersSeparatelyForEachFlyer() {
        Permanent first = setUpAttack(new AirElemental());
        Permanent second = addCreatureReady(player2, new AirElemental());
        Permanent ground = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1, 2));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).extracting(StackEntry::getTargetId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(first.getMarkedDamage()).isEqualTo(4);
        assertThat(second.getMarkedDamage()).isEqualTo(4);
        assertThat(ground.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Losing flying after the trigger does not prevent the damage")
    void losingFlyingAfterDeclarationStillDealsDamage() {
        Permanent attacker = setUpAttack(new AirElemental());
        declareAttackers(player2, List.of(0));
        attacker.getRemovedKeywords().add(Keyword.FLYING);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("The trigger deals damage even after Raking Canopy leaves the battlefield")
    void sourceLeavingDoesNotStopDamage() {
        Permanent attacker = setUpAttack(new AirElemental());
        declareAttackers(player2, List.of(0));
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Shroud does not prevent the non-targeting trigger from dealing damage")
    void shroudDoesNotPreventDamage() {
        Permanent attacker = setUpAttack(new AirElemental());
        attacker.getGrantedKeywords().add(Keyword.SHROUD);
        declareAttackers(player2, List.of(0));

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
    }
}
