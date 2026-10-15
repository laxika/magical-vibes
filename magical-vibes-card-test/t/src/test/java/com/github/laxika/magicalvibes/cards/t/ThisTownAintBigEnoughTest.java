package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BoomBox;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThisTownAintBigEnough.class, GrizzlyBears.class, Forest.class, BoomBox.class})
class ThisTownAintBigEnoughTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two target nonland permanents")
    void returnsTwoTargetNonlandPermanents() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ThisTownAintBigEnough()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of(mine.getId(), theirs.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Uses the reduced cost when the controlled target is chosen second")
    void usesReducedCostWhenControlledTargetIsSecond() {
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ThisTownAintBigEnough()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of(theirs.getId(), mine.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Requires the full cost when no target is controlled")
    void requiresFullCostWithoutControlledTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ThisTownAintBigEnough()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ThisTownAintBigEnough()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    void canCastWithoutTargetsAtFullCost() {
        harness.addToBattlefield(player1, new BoomBox());
        harness.setHand(player1, List.of(new ThisTownAintBigEnough()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of());

        harness.assertOnBattlefield(player1, "Boom Box");
        harness.assertInGraveyard(player1, "This Town Ain't Big Enough");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void returnsSingleControlledArtifactAtReducedCost() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BoomBox());
        harness.setHand(player1, List.of(new ThisTownAintBigEnough()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Boom Box");
        harness.assertInHand(player1, "Boom Box");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void twoControlledTargetsReduceCostOnlyOnce() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BoomBox());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BoomBox());
        harness.setHand(player1, List.of(new ThisTownAintBigEnough()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof BoomBox).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void cannotUseDiscountForOnlyOpponentTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BoomBox());
        harness.setHand(player1, List.of(new ThisTownAintBigEnough()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Boom Box");
        harness.assertInHand(player1, "This Town Ain't Big Enough");
    }

    @Test
    void cannotUseDiscountWithoutTargets() {
        harness.addToBattlefield(player1, new BoomBox());
        harness.setHand(player1, List.of(new ThisTownAintBigEnough()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void remainingTargetReturnsAfterControlledTargetLeavesInResponse() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new BoomBox());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new BoomBox());
        harness.setHand(player1, List.of(new ThisTownAintBigEnough(), new ThisTownAintBigEnough()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of(mine.getId(), theirs.getId()));
        harness.castAndResolveInstant(player1, 0, mine.getId());
        harness.assertOnBattlefield(player2, "Boom Box");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Boom Box");
        harness.assertInHand(player2, "Boom Box");
        harness.assertNotOnBattlefield(player2, "Boom Box");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
