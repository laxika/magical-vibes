package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisplaceTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and immediately returns up to two targeted creatures")
    void flickersTwoCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AngelOfMercy());
        harness.setHand(player1, List.of(new Displace()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID angelId = harness.getPermanentId(player1, "Angel of Mercy");

        harness.castInstant(player1, 0, List.of(bearsId, angelId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Angel of Mercy");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bearsId);
        assertThat(harness.getPermanentId(player1, "Angel of Mercy")).isNotEqualTo(angelId);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returns a stolen creature under its owner's control")
    void returnsUnderOwnerControl() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castEnchantment(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Displace()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Allows choosing no creatures")
    void allowsNoTargets() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Displace()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Displace");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Displace()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBearsId))
                .isInstanceOf(IllegalStateException.class);
    }
}
