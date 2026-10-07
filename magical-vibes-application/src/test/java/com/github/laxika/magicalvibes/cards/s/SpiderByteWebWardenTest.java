package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiderByteWebWarden.class, Forest.class, GrizzlyBears.class})
class SpiderByteWebWardenTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns up to one target nonland permanent to its owner's hand")
    void etbBouncesTargetNonlandPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castSpiderByte(targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Spider-Byte, Web Warden");
    }

    @Test
    @DisplayName("Can enter without choosing a target")
    void canChooseNoTarget() {
        harness.castFromHand(player1, new SpiderByteWebWarden(), "{2}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();

        harness.assertOnBattlefield(player1, "Spider-Byte, Web Warden");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new SpiderByteWebWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Can decline to bounce even when a legal target exists")
    void canChooseNoTargetWithLegalTarget() {
        harness.addToBattlefield(player2, new SpiderByteWebWarden());

        harness.castFromHand(player1, new SpiderByteWebWarden(), "{2}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();

        harness.assertOnBattlefield(player1, "Spider-Byte, Web Warden");
        harness.assertOnBattlefield(player2, "Spider-Byte, Web Warden");
        harness.assertNotInHand(player2, "Spider-Byte, Web Warden");
    }

    @Test
    @DisplayName("Can return a permanent you control")
    void canBounceOwnPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castSpiderByte(harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Spider-Byte, Web Warden");
    }

    @Test
    @DisplayName("Returns a stolen permanent to its owner's hand")
    void returnsToOwnerRatherThanController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gd.stolenCreatures.put(targetId, player1.getId());

        castSpiderByte(targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target Spider-Byte itself after it enters")
    void canBounceItself() {
        harness.castFromHand(player1, new SpiderByteWebWarden(), "{2}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Spider-Byte, Web Warden"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spider-Byte, Web Warden");
        harness.assertInHand(player1, "Spider-Byte, Web Warden");
    }

    private void castSpiderByte(UUID targetId) {
        harness.setHand(player1, List.of(new SpiderByteWebWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
