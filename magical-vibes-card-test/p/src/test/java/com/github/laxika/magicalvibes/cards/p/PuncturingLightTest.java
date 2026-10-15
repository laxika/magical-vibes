package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.cards.t.ThornhideWolves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PuncturingLight.class, QuilledWolf.class, ThornhideWolves.class})
class PuncturingLightTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target attacking creature with power 3 or less")
    void destroysSmallAttacker() {
        Permanent attacker = addAttacker(player2, new QuilledWolf());

        prepare();
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertNotOnBattlefield(player2, "Quilled Wolf");
        harness.assertInGraveyard(player2, "Quilled Wolf");
    }

    @Test
    @DisplayName("Destroys a target blocking creature with power 3 or less")
    void destroysSmallBlocker() {
        Permanent blocker = addBlocker(player2, new QuilledWolf());

        prepare();
        harness.castAndResolveInstant(player1, 0, blocker.getId());

        harness.assertNotOnBattlefield(player2, "Quilled Wolf");
        harness.assertInGraveyard(player2, "Quilled Wolf");
    }

    @Test
    @DisplayName("Cannot target a creature with power 4")
    void cannotTargetLargeCreature() {
        Permanent attacker = addAttacker(player2, new ThornhideWolves());

        prepare();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a small creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new QuilledWolf()).getId();

        prepare();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys an attacker with effective power exactly three")
    void destroysAttackerAtPowerLimit() {
        Permanent attacker = addAttacker(player2, new QuilledWolf());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        prepare();
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertNotOnBattlefield(player2, "Quilled Wolf");
        harness.assertInGraveyard(player2, "Quilled Wolf");
    }

    @Test
    @DisplayName("Cannot target a blocker with power four")
    void cannotTargetLargeBlocker() {
        Permanent blocker = addBlocker(player2, new ThornhideWolves());

        prepare();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a printed small attacker whose effective power exceeds three")
    void cannotTargetBoostedAttacker() {
        Permanent attacker = addAttacker(player2, new QuilledWolf());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        prepare();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not destroy a target whose power exceeds three before resolution")
    void targetGrowsBeforeResolution() {
        Permanent attacker = addAttacker(player2, new QuilledWolf());
        cast(attacker.getId());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Quilled Wolf");
        harness.assertNotInGraveyard(player2, "Quilled Wolf");
        harness.assertInGraveyard(player1, "Puncturing Light");
    }

    @Test
    @DisplayName("Does not destroy a target that has left combat before resolution")
    void targetLeavesCombatBeforeResolution() {
        Permanent attacker = addAttacker(player2, new QuilledWolf());
        cast(attacker.getId());
        attacker.setAttacking(false);
        attacker.setAttackTarget(null);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Quilled Wolf");
        harness.assertNotInGraveyard(player2, "Quilled Wolf");
        harness.assertInGraveyard(player1, "Puncturing Light");
    }

    @Test
    @DisplayName("Can destroy its controller's attacking creature")
    void destroysOwnAttacker() {
        Permanent attacker = addAttacker(player1, new QuilledWolf());
        attacker.setAttackTarget(player2.getId());

        prepare();
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Quilled Wolf");
        harness.assertInGraveyard(player1, "Quilled Wolf");
    }

    @Test
    @DisplayName("Destruction does not remove an indestructible attacker")
    void indestructibleAttackerSurvives() {
        Permanent attacker = addAttacker(player2, new QuilledWolf());
        attacker.setCounterCount(CounterType.INDESTRUCTIBLE, 1);

        prepare();
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertOnBattlefield(player2, "Quilled Wolf");
        harness.assertNotInGraveyard(player2, "Quilled Wolf");
        harness.assertInGraveyard(player1, "Puncturing Light");
    }
    private void prepare() {
        harness.setHand(player1, List.of(new PuncturingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void cast(UUID targetId) {
        prepare();
        harness.castInstant(player1, 0, targetId);
    }

    private Permanent addAttacker(Player owner, Card card) {
        Permanent attacker = combatCreature(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(owner.equals(player1) ? player2.getId() : player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner, Card card) {
        Permanent blocker = combatCreature(owner, card);
        blocker.setBlocking(true);
        Player attackingPlayer = owner.equals(player1) ? player2 : player1;
        Permanent attacker = addAttacker(attackingPlayer, new QuilledWolf());
        blocker.addBlockingTargetId(attacker.getId());
        return blocker;
    }

    private Permanent combatCreature(Player owner, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(owner, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
