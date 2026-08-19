package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeldonArsonistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices two lands to destroy the target land")
    void sacrificesTwoLandsToDestroyTargetLand() {
        harness.addToBattlefield(player1, new KeldonArsonist());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID targetId = harness.getPermanentId(player2, "Forest");

        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Keldon Arsonist");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Mountain"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Mountain"))
                .hasSize(2);
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot activate without two lands to sacrifice")
    void cannotActivateWithoutTwoLands() {
        harness.addToBattlefield(player1, new KeldonArsonist());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new KeldonArsonist());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
