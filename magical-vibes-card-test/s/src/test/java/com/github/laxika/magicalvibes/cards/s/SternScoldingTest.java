package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GarruksCompanion;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GarruksCompanion.class, HillGiant.class, SternScolding.class})
class SternScoldingTest extends BaseCardTest {

    @Test
    void countersCreatureSpellWithPowerOrToughnessAtMostTwo() {
        GarruksCompanion companion = new GarruksCompanion();
        harness.setHand(player1, List.of(companion));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SternScolding()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, companion.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Garruk's Companion");
        harness.assertNotOnBattlefield(player1, "Garruk's Companion");
    }

    @Test
    void cannotTargetCreatureSpellWithBothStatsAboveTwo() {
        HillGiant hillGiant = new HillGiant();
        harness.setHand(player1, List.of(hillGiant));
        harness.addMana(player1, ManaColor.RED, 4);

        SternScolding sternScolding = new SternScolding();
        harness.setHand(player2, List.of(sternScolding));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, hillGiant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power or toughness 2 or less");
        assertThat(harness.getGameData().playerHands.get(player2.getId()))
                .containsExactly(sternScolding);
    }

    @Test
    void fizzlesIfTheTargetCreatureSpellLeavesTheStack() {
        GarruksCompanion companion = new GarruksCompanion();
        harness.setHand(player1, List.of(companion));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SternScolding()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, companion.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(entry -> entry.getCard().getId().equals(companion.getId()));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Stern Scolding");
    }
}
