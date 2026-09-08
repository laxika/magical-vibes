package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BitingRainTest extends BaseCardTest {

    @Test
    @DisplayName("Gives every creature -2/-2")
    void weakensAllCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new AvatarOfMight());
        Permanent opposingCreature = addCreatureReady(player2, new AvatarOfMight());
        castBitingRain();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(6);
    }

    @Test
    @DisplayName("The -2/-2 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent opposingCreature = addCreatureReady(player2, new AvatarOfMight());
        castBitingRain();

        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(8);
    }

    private void castBitingRain() {
        harness.setHand(player1, java.util.List.of(new BitingRain()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }
}
