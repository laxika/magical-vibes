package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirCultElemental.class, Forest.class, GrizzlyBears.class})
class AirCultElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to one other target creature to its owner's hand")
    void returnsTargetCreatureToItsOwnersHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAirCultElemental(creature.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Air-Cult Elemental");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can decline the optional creature target")
    void canDeclineTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AirCultElemental()));
        addManaForAirCultElemental();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Air-Cult Elemental");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AirCultElemental()));
        addManaForAirCultElemental();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature");
    }

    private void castAirCultElemental(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new AirCultElemental()));
        addManaForAirCultElemental();
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void addManaForAirCultElemental() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
