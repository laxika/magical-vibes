package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DarkBetrayalTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a targeted black creature")
    void destroysBlackCreature() {
        harness.addToBattlefield(player2, new BogImp());
        UUID target = harness.getPermanentId(player2, "Bog Imp");

        harness.setHand(player1, List.of(new DarkBetrayal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, List.of(target));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bog Imp");
    }

    @Test
    @DisplayName("Cannot target a nonblack creature")
    void cannotTargetNonblackCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID target = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new DarkBetrayal()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new Forest());
        UUID target = harness.getPermanentId(player2, "Forest");

        harness.setHand(player1, List.of(new DarkBetrayal()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target)))
                .isInstanceOf(IllegalStateException.class);
    }
}
