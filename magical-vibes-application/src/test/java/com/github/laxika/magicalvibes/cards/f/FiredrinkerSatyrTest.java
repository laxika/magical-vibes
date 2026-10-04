package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiredrinkerSatyr.class, Shock.class})
class FiredrinkerSatyrTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever Firedrinker Satyr is dealt damage, its controller takes that much damage")
    void damageTakenIsReflectedToController() {
        harness.addToBattlefield(player2, new FiredrinkerSatyr());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Firedrinker Satyr"));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Firedrinker Satyr");
    }

    @Test
    @DisplayName("The activated ability boosts the Satyr and deals 1 damage to its controller")
    void activatedAbilityBoostsAndDealsDamage() {
        Permanent satyr = addCreatureReady(player1, new FiredrinkerSatyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, satyr)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, satyr)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The activated boost wears off at end of turn")
    void activatedBoostWearsOffAtEndOfTurn() {
        Permanent satyr = addCreatureReady(player1, new FiredrinkerSatyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, satyr)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, satyr)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability can be activated while summoning sick and its boosts accumulate")
    void repeatedActivationsWhileSummoningSick() {
        Permanent satyr = harness.addToBattlefieldAndReturn(player1, new FiredrinkerSatyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, satyr)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, satyr)).isEqualTo(1);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The activated ability still deals damage after the Satyr dies in response")
    void activatedAbilityDealsDamageAfterSourceDies() {
        harness.addToBattlefield(player1, new FiredrinkerSatyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Firedrinker Satyr"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Firedrinker Satyr");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Lethal combat damage triggers each Satyr for the full damage received")
    void lethalCombatDamageTriggersBothSatyrs() {
        addCreatureReady(player1, new FiredrinkerSatyr());
        addCreatureReady(player2, new FiredrinkerSatyr());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Firedrinker Satyr");
        harness.assertInGraveyard(player2, "Firedrinker Satyr");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }
}
