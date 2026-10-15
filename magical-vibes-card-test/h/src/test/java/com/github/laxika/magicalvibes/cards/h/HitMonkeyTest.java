package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HitMonkey.class, Cancel.class, GiantGrowth.class, SerraAngel.class})
class HitMonkeyTest extends BaseCardTest {

    @Test
    @DisplayName("This spell can't be countered")
    void cannotBeCountered() {
        HitMonkey hitMonkey = new HitMonkey();
        harness.setHand(player1, List.of(hitMonkey));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, hitMonkey.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hit-Monkey");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void canAttackImmediatelyWithoutTapping() {
        harness.setHand(player1, List.of(new HitMonkey()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent monkey = findPermanent(player1, "Hit-Monkey");
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(monkey.isAttacking()).isTrue();
        assertThat(monkey.isTapped()).isFalse();
        resolveCombat();
        harness.assertLife(player2, 17);
    }

    @Test
    void canBlockFlyingCreatureAndDestroyItWithDeathtouch() {
        addCreatureReady(player1, new SerraAngel());
        harness.addToBattlefield(player2, new HitMonkey());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Serra Angel");
        harness.assertInGraveyard(player2, "Hit-Monkey");
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentCannotTargetWithGiantGrowth() {
        Permanent monkey = harness.addToBattlefieldAndReturn(player1, new HitMonkey());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, monkey.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        harness.assertInHand(player2, "Giant Growth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerCanTargetWithGiantGrowth() {
        Permanent monkey = harness.addToBattlefieldAndReturn(player1, new HitMonkey());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, monkey.getId());
        declareAttackersAndPrepareBlockers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
        harness.assertInGraveyard(player1, "Giant Growth");
    }
}
