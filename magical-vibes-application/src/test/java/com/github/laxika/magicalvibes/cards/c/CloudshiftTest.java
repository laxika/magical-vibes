package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CloudshiftTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and immediately returns the targeted creature")
    void flickersOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        // It is a new object, so summoning sickness applies again.
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Returned creature comes back under the spell's controller, not the owner")
    void returnsUnderYourControl() {
        // Lay Claim leaves the bears owned by player2 but controlled by player1. Stealing it with a
        // real Aura keeps the ownership record and the layer-2 control effect in step; writing only
        // gd.stolenCreatures by hand would let state-based actions hand the bears back to player2.
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castEnchantment(player1, 0, bearsId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        // The Aura falls off the flickered creature, but it still returns under player1's control.
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature you do not control")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, bearsId);

        Permanent bearsPerm = gqs.findPermanentById(gd, bearsId);
        gd.playerBattlefields.get(player1.getId()).remove(bearsPerm);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
