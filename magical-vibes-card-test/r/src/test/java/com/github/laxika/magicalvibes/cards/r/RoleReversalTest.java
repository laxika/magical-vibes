package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.d.DivinersWand;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoleReversal.class, GrizzlyBears.class, HillGiant.class, Forest.class, Plains.class,
        Millstone.class, Bitterblossom.class, DivinersWand.class})
class RoleReversalTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new RoleReversal()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    @Test
    @DisplayName("Exchanges control of two target creatures")
    void exchangesCreatures() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opponent.getId()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Exchanges control of two target lands")
    void exchangesLands() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opponent.getId()));

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Rejects targets that share no permanent type")
    void rejectsUnrelatedPermanentTypes() {
        prepare();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("share a card type");
    }

    @Test
    @DisplayName("Kindred alone does not satisfy the shared permanent type restriction")
    void rejectsSharedKindredWithoutSharedPermanentType() {
        prepare();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DivinersWand());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(enchantment.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exchanges control with the opponent's permanent selected first")
    void exchangesWithOpponentTargetFirst() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castAndResolveSorcery(player1, 0, List.of(opponent.getId(), own.getId()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Does nothing when both targets have the same controller")
    void doesNothingWithSameController() {
        prepare();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Neither half of the exchange occurs when one target leaves before resolution")
    void doesNothingWhenTargetLeaves() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castSorcery(player1, 0, List.of(own.getId(), opponent.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opponent);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
