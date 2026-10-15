package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.h.HeatRay;
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

@CardUsed({Lull.class, ArgothianSwine.class, HeatRay.class})
class LullTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all combat damage after resolving")
    void preventsAllCombatDamage() {
        harness.setHand(player1, List.of(new Lull()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("An unblocked attacker deals no damage while Lull is in effect")
    void unblockedAttackerDealsNoDamage() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Lull()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        Permanent attacker = addCreatureReady(player2, new ArgothianSwine());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents combat damage to creatures as well as players")
    void preventsCombatDamageToCreatures() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Lull()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        Permanent attacker = addCreatureReady(player2, new ArgothianSwine());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player1, new ArgothianSwine());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Cycling discards Lull and draws one card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Lull()));
        harness.setLibrary(player1, List.of(new ArgothianSwine()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lull");
        harness.assertInHand(player1, "Argothian Swine");
    }

    @Test
    @DisplayName("Lull does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        Permanent creature = addCreatureReady(player2, new ArgothianSwine());
        harness.setHand(player1, List.of(new Lull(), new HeatRay()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, 3, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Argothian Swine");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Combat damage prevention expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Lull()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        Permanent attacker = addCreatureReady(player2, new ArgothianSwine());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cycling discards immediately, draws only on resolution, and does not prevent damage")
    void cyclingDoesNotResolveSpellEffect() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Lull()));
        harness.setLibrary(player1, List.of(new ArgothianSwine()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Lull");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Argothian Swine");

        Permanent attacker = addCreatureReady(player2, new ArgothianSwine());
        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new Lull()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Lull");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
