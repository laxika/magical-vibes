package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoshIronGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the sacrificed artifact's mana value")
    void dealsDamageEqualToSacrificedArtifactManaValue() {
        harness.addToBattlefield(player1, new BoshIronGolem());
        harness.addToBattlefield(player1, new DarksteelIngot());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent bosh = findPermanent(player1, "Bosh, Iron Golem");
        Permanent ingot = findPermanent(player1, "Darksteel Ingot");
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, ingot.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Darksteel Ingot");
        harness.assertOnBattlefield(player1, "Bosh, Iron Golem");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bosh);
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
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new BoshIronGolem());
        harness.addToBattlefield(player1, new DarksteelIngot());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
