package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SoulswornSpirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RendFleshTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a non-Spirit creature")
    void destroysNonSpiritCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID target = harness.getPermanentId(player2, "Hill Giant");

        harness.setHand(player1, List.of(new RendFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Cannot target a Spirit")
    void cannotTargetSpirit() {
        harness.addToBattlefield(player2, new SoulswornSpirit());
        UUID spirit = harness.getPermanentId(player2, "Soulsworn Spirit");

        harness.setHand(player1, List.of(new RendFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, spirit))
                .isInstanceOf(IllegalStateException.class);
    }
}
