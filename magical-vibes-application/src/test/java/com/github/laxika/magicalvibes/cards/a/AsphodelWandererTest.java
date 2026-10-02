package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.s.SipOfHemlock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AsphodelWanderer.class, NessianCourser.class, SipOfHemlock.class})
class AsphodelWandererTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration grants a regeneration shield")
    void activatingRegenerationGrantsShield() {
        Permanent wanderer = addWandererReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(wanderer.getId());

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Asphodel Wanderer").getRegenerationShield()).isEqualTo(1);
        assertThat(wanderer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A regeneration shield saves Asphodel Wanderer from lethal damage")
    void regenerationShieldSavesFromLethalDamage() {
        addWandererReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wanderer = findPermanent(player1, "Asphodel Wanderer");
        wanderer.setBlocking(true);
        wanderer.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Asphodel Wanderer")).isNotNull();
        assertThat(findPermanent(player1, "Asphodel Wanderer").getRegenerationShield()).isZero();
        assertThat(wanderer.isTapped()).isTrue();
        assertThat(wanderer.isBlocking()).isFalse();
        assertThat(wanderer.getMarkedDamage()).isZero();
    }

    private Permanent addWandererReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AsphodelWanderer());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Regeneration can be activated while tapped and summoning sick")
    void regenerationDoesNotRequireTapOrHaste() {
        Permanent wanderer = harness.addToBattlefieldAndReturn(player1, new AsphodelWanderer());
        wanderer.setSummoningSick(true);
        wanderer.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wanderer.getRegenerationShield()).isEqualTo(1);
        assertThat(wanderer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Regeneration replaces destruction but does not prevent the destroy spell's life loss")
    void regenerationReplacesDestruction() {
        Permanent wanderer = addWandererReady(player2);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SipOfHemlock()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, wanderer.getId());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(wanderer.getRegenerationShield()).isEqualTo(1);
        assertThat(wanderer.isTapped()).isFalse();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Asphodel Wanderer");
        assertThat(wanderer.getRegenerationShield()).isZero();
        assertThat(wanderer.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
