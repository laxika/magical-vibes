package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.r.Refocus;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrostWalker.class, IcyManipulator.class, Refocus.class, Shock.class})
class FrostWalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Frost Walker sacrifices itself when targeted by a spell")
    void sacrificesWhenTargetedBySpell() {
        Permanent frostWalker = harness.addToBattlefieldAndReturn(player1, new FrostWalker());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, frostWalker.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Frost Walker");
        harness.assertInGraveyard(player1, "Frost Walker");
    }

    @Test
    @DisplayName("Frost Walker sacrifices itself when targeted by an activated ability")
    void sacrificesWhenTargetedByAbility() {
        Permanent frostWalker = harness.addToBattlefieldAndReturn(player1, new FrostWalker());

        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        icyManipulator.setSummoningSick(false);

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(icyManipulator),
                null, frostWalker.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Frost Walker");
        harness.assertInGraveyard(player1, "Frost Walker");
    }

    @Test
    @DisplayName("A friendly spell triggers sacrifice before resolving and cannot draw with an illegal target")
    void friendlySpellTriggersSacrificeBeforeItResolves() {
        Permanent frostWalker = harness.addToBattlefieldAndReturn(player1, new FrostWalker());
        harness.setLibrary(player1, List.of(new FrostWalker()));
        harness.setHand(player1, List.of(new Refocus()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, frostWalker.getId());

        harness.assertOnBattlefield(player1, "Frost Walker");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Frost Walker");
        harness.assertInGraveyard(player1, "Frost Walker");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Refocus");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A spell targeting its controller does not cause Frost Walker to be sacrificed")
    void doesNotSacrificeWhenItsControllerIsTargeted() {
        harness.addToBattlefield(player1, new FrostWalker());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Frost Walker");
        harness.assertNotInGraveyard(player1, "Frost Walker");
    }

    @Test
    @DisplayName("Targeting one Frost Walker sacrifices only that permanent")
    void sacrificesOnlyTheTargetedWalker() {
        Permanent targeted = harness.addToBattlefieldAndReturn(player1, new FrostWalker());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new FrostWalker());
        harness.setHand(player1, List.of(new Refocus()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, targeted.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).containsExactly(other.getId());
        harness.assertInGraveyard(player1, "Frost Walker");
    }
}
