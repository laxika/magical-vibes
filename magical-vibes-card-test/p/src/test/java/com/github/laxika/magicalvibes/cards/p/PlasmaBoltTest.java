package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StarfieldShepherd;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({PlasmaBolt.class, Forest.class, StarfieldShepherd.class})
class PlasmaBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage without a Void event")
    void dealsTwoDamageWithoutVoid() {
        castPlasmaBolt();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals 3 damage after a nonland permanent left the battlefield")
    void dealsThreeDamageAfterNonlandPermanentLeft() {
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new StarfieldShepherd());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        castPlasmaBolt();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Does not get the Void bonus after only a land left the battlefield")
    void dealsTwoDamageAfterOnlyLandLeft() {
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        castPlasmaBolt();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals 3 damage after a spell was warped")
    void dealsThreeDamageAfterWarpedSpell() {
        harness.setHand(player1, List.of(new StarfieldShepherd()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        castPlasmaBolt();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Can deal lethal damage to a creature without a prior Void event")
    void killsCreatureWithoutVoid() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StarfieldShepherd());
        harness.setHand(player1, List.of(new PlasmaBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Starfield Shepherd");
        harness.assertInGraveyard(player2, "Starfield Shepherd");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Checks Void at resolution after a permanent leaves while the spell is pending")
    void checksVoidAtResolution() {
        Permanent departed = harness.addToBattlefieldAndReturn(player1, new StarfieldShepherd());
        harness.setHand(player1, List.of(new PlasmaBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Does not redirect damage when its creature target leaves before resolution")
    void doesNotDealDamageWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StarfieldShepherd());
        harness.setHand(player1, List.of(new PlasmaBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Plasma Bolt");
    }

    private void castPlasmaBolt() {
        harness.setHand(player1, List.of(new PlasmaBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
