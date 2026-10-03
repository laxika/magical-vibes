package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SterlingHound;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorruptedConviction.class, SterlingHound.class, Swamp.class})
class CorruptedConvictionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature as an additional cost and draws two cards")
    void sacrificesCreatureAndDrawsTwoCards() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SterlingHound());

        harness.setHand(player1, List.of(new CorruptedConviction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sterling Hound");
        harness.assertInGraveyard(player1, "Sterling Hound");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 2);
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        harness.setHand(player1, List.of(new CorruptedConviction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SterlingHound());

        harness.setHand(player1, List.of(new CorruptedConviction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature permanent")
    void cannotSacrificeNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setHand(player1, List.of(new CorruptedConviction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertInHand(player1, "Corrupted Conviction");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution, even for a tapped summoning-sick creature")
    void paysSacrificeBeforeDrawing() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SterlingHound());
        sacrifice.tap();
        sacrifice.setSummoningSick(true);
        harness.setHand(player1, List.of(new CorruptedConviction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Sterling Hound");
        harness.assertInGraveyard(player1, "Sterling Hound");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 2);
        harness.assertInGraveyard(player1, "Corrupted Conviction");
        assertThat(gd.stack).isEmpty();
    }
}
