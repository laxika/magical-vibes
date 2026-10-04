package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CentaurNurturer;
import com.github.laxika.magicalvibes.cards.c.CharmedStray;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Blindblast.class, Forest.class, CentaurNurturer.class, CharmedStray.class})
class BlindblastTest extends BaseCardTest {

    @Test
    void dealsDamagePreventsBlockingAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurNurturer());
        harness.setHand(player1, List.of(new Blindblast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size() - 1;

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterCast + 1);
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new CentaurNurturer());
        harness.setHand(player1, List.of(new Blindblast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void fizzlesWithoutDrawingIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurNurturer());
        harness.setHand(player1, List.of(new Blindblast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        int handSizeBeforeResolution = gd.playerHands.get(player1.getId()).size();
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeResolution);
        harness.assertInGraveyard(player1, "Blindblast");
    }

    @Test
    void drawsEvenWhenDamageKillsTheTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CharmedStray());
        harness.setHand(player1, List.of(new Blindblast()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Charmed Stray");
        harness.assertInGraveyard(player2, "Charmed Stray");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void canTargetItsControllersCreatureAndOnlyRestrictsThatCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CentaurNurturer());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CentaurNurturer());
        harness.setHand(player1, List.of(new Blindblast()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(other.getMarkedDamage()).isZero();
        assertThat(other.isCantBlockThisTurn()).isFalse();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void targetCannotBeDeclaredAsABlocker() {
        Permanent attacker = addCreatureReady(player1, new CentaurNurturer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurNurturer());
        harness.setHand(player1, List.of(new Blindblast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());

        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    void blockingRestrictionExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurNurturer());
        harness.setHand(player1, List.of(new Blindblast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(target.getMarkedDamage()).isZero();
    }
}
