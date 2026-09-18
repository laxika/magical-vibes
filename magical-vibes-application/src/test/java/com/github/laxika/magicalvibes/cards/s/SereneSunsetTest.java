package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JeskaWarriorAdept;
import com.github.laxika.magicalvibes.cards.r.RiftstonePortal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JeskaWarriorAdept.class, RiftstonePortal.class, SereneSunset.class, SuntailHawk.class})
class SereneSunsetTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from the targeted creatures only")
    void preventsCombatDamageFromTargetedCreaturesOnly() {
        harness.setLife(player2, 20);
        Permanent targeted = addAttacker(player1);
        addAttacker(player1);

        harness.setHand(player1, List.of(new SereneSunset()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantForX(player1, 0, 1, List.of(targeted.getId()));
        harness.passBothPriorities();

        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Rejects more creature targets than X")
    void rejectsMoreTargetsThanX() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new SereneSunset()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantForX(
                player1, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects fewer creature targets than X")
    void rejectsFewerTargetsThanX() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new SereneSunset()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantForX(
                player1, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents combat damage from every targeted creature")
    void preventsCombatDamageFromEachTargetedCreature() {
        harness.setLife(player2, 20);
        Permanent firstTarget = addAttacker(player1);
        Permanent secondTarget = addAttacker(player1);
        addAttacker(player1);

        harness.setHand(player1, List.of(new SereneSunset()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstantForX(player1, 0, 2, List.of(firstTarget.getId(), secondTarget.getId()));
        harness.passBothPriorities();

        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void canTargetOpponentsCreature() {
        harness.setLife(player1, 20);
        Permanent target = addAttacker(player2);

        harness.setHand(player1, List.of(new SereneSunset()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantForX(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.setHand(player1, List.of(new SereneSunset()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent land = harness.addToBattlefieldAndReturn(player1, new RiftstonePortal());

        assertThatThrownBy(() -> harness.castInstantForX(
                player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=0 requires no creature targets")
    void zeroXRequiresNoTargets() {
        harness.setLife(player2, 20);
        addAttacker(player1);

        harness.setHand(player1, List.of(new SereneSunset()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        harness.setLife(player2, 20);
        Permanent targeted = addCreatureReady(player1, new JeskaWarriorAdept());

        harness.setHand(player1, List.of(new SereneSunset()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantForX(player1, 0, 1, List.of(targeted.getId()));
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Combat damage prevention ends at the end of the turn")
    void preventionEndsAtEndOfTurn() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1);

        harness.setHand(player1, List.of(new SereneSunset()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantForX(player1, 0, 1, List.of(attacker.getId()));
        harness.passBothPriorities();

        resolveCombat(player1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntil(player1, TurnStep.UPKEEP);
        attacker.setAttacking(true);
        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private Permanent addAttacker(Player player) {
        Permanent attacker = addCreatureReady(player, new SuntailHawk());
        attacker.setAttacking(true);
        return attacker;
    }

    @Test
    @DisplayName("Prevents combat damage from each of multiple targeted creatures")
    void preventsCombatDamageFromEachTarget() {
        harness.setLife(player2, 20);
        Permanent firstTarget = addAttackerForJudReview();
        Permanent secondTarget = addAttackerForJudReview();
        addAttackerForJudReview();

        harness.setHand(player1, List.of(new SereneSunset()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstantForX(player1, 0, 2, List.of(firstTarget.getId(), secondTarget.getId()));
        harness.passBothPriorities();

        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Requires exactly X creature targets")
    void requiresExactlyXTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new SereneSunset()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantForX(
                player1, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=0 requires no targets and has no effect")
    void zeroXHasNoTargetsAndNoEffect() {
        harness.setLife(player2, 20);
        addAttackerForJudReview();

        harness.setHand(player1, List.of(new SereneSunset()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private Permanent addAttackerForJudReview() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }
}
