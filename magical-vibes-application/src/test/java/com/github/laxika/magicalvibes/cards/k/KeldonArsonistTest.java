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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Rhystic Cave"));
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
}
