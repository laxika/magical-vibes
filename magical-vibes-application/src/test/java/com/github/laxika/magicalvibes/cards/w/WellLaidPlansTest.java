package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.s.SamiteArcher;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.cards.z.Zap;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WellLaidPlans.class, SamiteArcher.class, AngelOfMercy.class, RagingKavu.class, Zap.class})
class WellLaidPlansTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents noncombat damage from a creature with a shared color")
    void preventsNoncombatDamageWithSharedColor() {
        harness.addToBattlefield(player1, new WellLaidPlans());
        addCreatureReady(player1, new SamiteArcher());
        Permanent target = addCreatureReady(player2, new AngelOfMercy());

        harness.activateAbility(player1, 1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Allows noncombat damage when the creatures do not share a color")
    void allowsNoncombatDamageWithoutSharedColor() {
        harness.addToBattlefield(player1, new WellLaidPlans());
        addCreatureReady(player1, new SamiteArcher());
        Permanent target = addCreatureReady(player2, new RagingKavu());

        harness.activateAbility(player1, 1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not prevent creature damage dealt to a player")
    void allowsCreatureDamageToPlayer() {
        harness.addToBattlefield(player1, new WellLaidPlans());
        addCreatureReady(player1, new SamiteArcher());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not prevent damage dealt to a creature by a noncreature source")
    void allowsNoncreatureDamageWithSharedColor() {
        harness.addToBattlefield(player1, new WellLaidPlans());
        Permanent target = addCreatureReady(player2, new RagingKavu());
        harness.setHand(player1, List.of(new Zap()));
        harness.setLibrary(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Stops preventing damage when Well-Laid Plans is face down")
    void stopsPreventingDamageWhenFaceDown() {
        Permanent plans = harness.addToBattlefieldAndReturn(player1, new WellLaidPlans());
        plans.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        addCreatureReady(player1, new SamiteArcher());
        Permanent target = addCreatureReady(player2, new AngelOfMercy());

        harness.activateAbility(player1, 1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Stops preventing damage when Well-Laid Plans loses all abilities")
    void stopsPreventingDamageWhenAbilitiesAreLost() {
        Permanent plans = harness.addToBattlefieldAndReturn(player1, new WellLaidPlans());
        plans.setLosesAllAbilitiesUntilEndOfTurn(true);
        addCreatureReady(player1, new SamiteArcher());
        Permanent target = addCreatureReady(player2, new AngelOfMercy());

        harness.activateAbility(player1, 1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents combat damage between creatures with a shared color")
    void preventsCombatDamageWithSharedColor() {
        harness.addToBattlefield(player1, new WellLaidPlans());
        Permanent blocker = addCreatureReady(player1, new AngelOfMercy());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new SamiteArcher());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Allows combat damage when the creatures do not share a color")
    void allowsCombatDamageWithoutSharedColor() {
        harness.addToBattlefield(player1, new WellLaidPlans());
        Permanent blocker = addCreatureReady(player1, new AngelOfMercy());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new RagingKavu());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player2, "Raging Kavu");
    }
}
