package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HumanFrailtyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a targeted Human creature")
    void destroysHumanCreature() {
        harness.addToBattlefield(player2, new EliteVanguard()); // Human Soldier
        UUID target = harness.getPermanentId(player2, "Elite Vanguard");

        harness.setHand(player1, List.of(new HumanFrailty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, List.of(target));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Elite Vanguard");
    }

    @Test
    @DisplayName("Cannot target a non-Human creature")
    void cannotTargetNonHumanCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // Bear
        UUID target = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new HumanFrailty()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new Forest());
        UUID land = harness.getPermanentId(player2, "Forest");

        harness.setHand(player1, List.of(new HumanFrailty()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land)))
                .isInstanceOf(IllegalStateException.class);
    }
}
