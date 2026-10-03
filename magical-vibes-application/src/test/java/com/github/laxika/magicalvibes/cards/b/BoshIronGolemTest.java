package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.c.CullingScales;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreatFurnace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoshIronGolem.class, AlphaMyr.class, CullingScales.class, Forest.class, GreatFurnace.class})
class BoshIronGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the sacrificed artifact's mana value")
    void dealsDamageEqualToSacrificedArtifactManaValue() {
        Permanent bosh = harness.addToBattlefieldAndReturn(player1, new BoshIronGolem());
        Permanent scales = harness.addToBattlefieldAndReturn(player1, new CullingScales());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, scales.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Culling Scales");
        harness.assertOnBattlefield(player1, "Bosh, Iron Golem");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bosh);
    }

    @Test
    @DisplayName("Can deal damage to a creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player1, new BoshIronGolem());
        Permanent scales = harness.addToBattlefieldAndReturn(player1, new CullingScales());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, scales.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Culling Scales");
        harness.assertInGraveyard(player2, "Alpha Myr");
    }

    @Test
    @DisplayName("May sacrifice itself as the artifact cost")
    void maySacrificeItself() {
        harness.addToBattlefield(player1, new BoshIronGolem());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        harness.assertInGraveyard(player1, "Bosh, Iron Golem");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-artifact as the cost")
    void cannotSacrificeNonArtifact() {
        harness.addToBattlefield(player1, new BoshIronGolem());
        Permanent scales = harness.addToBattlefieldAndReturn(player1, new CullingScales());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Bosh, Iron Golem");
        harness.assertOnBattlefield(player1, "Forest");

        harness.handlePermanentChosen(player1, scales.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Culling Scales");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new BoshIronGolem());
        harness.addToBattlefield(player1, new CullingScales());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificingArtifactLandDealsNoDamage() {
        harness.addToBattlefield(player1, new BoshIronGolem());
        Permanent furnace = harness.addToBattlefieldAndReturn(player1, new GreatFurnace());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, furnace.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Great Furnace");
        harness.assertOnBattlefield(player1, "Bosh, Iron Golem");
    }

    @Test
    void cannotSacrificeOpponentsArtifact() {
        harness.addToBattlefield(player1, new BoshIronGolem());
        Permanent scales = harness.addToBattlefieldAndReturn(player1, new CullingScales());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, scales.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Alpha Myr");
        harness.assertInGraveyard(player1, "Culling Scales");
    }

    @Test
    void stackedActivationsRetainTheirOwnSacrificedManaValuesAfterBoshLeaves() {
        harness.addToBattlefield(player1, new BoshIronGolem());
        Permanent scales = harness.addToBattlefieldAndReturn(player1, new CullingScales());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, scales.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Bosh, Iron Golem");
        harness.passBothPriorities();
        harness.assertLife(player2, 12);
        harness.passBothPriorities();

        harness.assertLife(player2, 9);
        harness.assertInGraveyard(player1, "Culling Scales");
    }
}
