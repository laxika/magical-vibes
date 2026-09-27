package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SnappingDrake;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PeelFromReality.class, Watchwolf.class, SnappingDrake.class, Forest.class})
class PeelFromRealityTest extends BaseCardTest {

    @Test
    @DisplayName("Returns both the controlled creature and the opponent's creature to their owners' hands")
    void bouncesBothCreatures() {
        Permanent watchwolf = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new SnappingDrake());
        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(watchwolf.getId(), drake.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Watchwolf");
        harness.assertInHand(player1, "Watchwolf");
        harness.assertNotOnBattlefield(player2, "Snapping Drake");
        harness.assertInHand(player2, "Snapping Drake");
    }

    @Test
    @DisplayName("Cannot target a creature you control as the second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new SnappingDrake());
        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature you don't control as the first target")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SnappingDrake());
        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(opponentCreature.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent as the first target")
    void cannotTargetNoncreatureAsFirstTarget() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SnappingDrake());
        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(ownLand.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent as the second target")
    void cannotTargetNoncreatureAsSecondTarget() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(ownCreature.getId(), opponentLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
