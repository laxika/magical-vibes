package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeldonArsonist.class, RhysticCave.class, PygmyRazorback.class})
class KeldonArsonistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices two lands to destroy the target land")
    void sacrificesTwoLandsToDestroyTargetLand() {
        harness.addToBattlefield(player1, new KeldonArsonist());
        harness.addToBattlefield(player1, new RhysticCave());
        harness.addToBattlefield(player1, new RhysticCave());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new RhysticCave());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, targetLand.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Keldon Arsonist");
        harness.assertNotOnBattlefield(player1, "Rhystic Cave");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Rhystic Cave"))
                .hasSize(2);
        harness.assertNotOnBattlefield(player2, "Rhystic Cave");
        harness.assertInGraveyard(player2, "Rhystic Cave");
    }

    @Test
    @DisplayName("Cannot activate without two lands to sacrifice")
    void cannotActivateWithoutTwoLands() {
        harness.addToBattlefield(player1, new KeldonArsonist());
        harness.addToBattlefield(player1, new RhysticCave());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new RhysticCave());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new KeldonArsonist());
        harness.addToBattlefield(player1, new RhysticCave());
        harness.addToBattlefield(player1, new RhysticCave());
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new PygmyRazorback());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped summoning-sick Arsonist pays its costs before the land is destroyed")
    void tappedSummoningSickArsonistPaysCostsBeforeResolution() {
        Permanent arsonist = harness.addToBattlefieldAndReturn(player1, new KeldonArsonist());
        arsonist.tap();
        arsonist.setSummoningSick(true);
        harness.addToBattlefield(player1, new RhysticCave());
        harness.addToBattlefield(player1, new RhysticCave());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new RhysticCave());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, targetLand.getId());

        harness.assertNotOnBattlefield(player1, "Rhystic Cave");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player2, "Rhystic Cave");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rhystic Cave");
        harness.assertOnBattlefield(player1, "Keldon Arsonist");
        assertThat(arsonist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target a land sacrificed to pay the cost without refunding the sacrifices")
    void canTargetLandSacrificedForCost() {
        harness.addToBattlefield(player1, new KeldonArsonist());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        harness.addToBattlefield(player1, new RhysticCave());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, targetLand.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rhystic Cave");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Keldon Arsonist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without paying the generic mana cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new KeldonArsonist());
        harness.addToBattlefield(player1, new RhysticCave());
        harness.addToBattlefield(player1, new RhysticCave());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new RhysticCave());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetLand.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player2, "Rhystic Cave");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
