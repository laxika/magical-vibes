package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BenalishEmissary;
import com.github.laxika.magicalvibes.cards.b.BenalishLancer;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Winnow.class, BenalishLancer.class, BenalishEmissary.class, Island.class})
class WinnowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys only the target when another same-named permanent exists, then draws")
    void destroysOnlyTargetWhenAnotherSameNamedPermanentExistsAndDraws() {
        harness.addToBattlefield(player2, new BenalishLancer());
        harness.addToBattlefield(player2, new BenalishLancer());
        harness.addToBattlefield(player1, new BenalishLancer());
        harness.addToBattlefield(player2, new BenalishEmissary());
        harness.setLibrary(player1, List.of(new BenalishEmissary()));

        UUID targetId = harness.getPermanentId(player2, "Benalish Lancer");
        castWinnow(targetId);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Benalish Lancer")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Benalish Lancer");
        harness.assertOnBattlefield(player2, "Benalish Emissary");
        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(card -> card.getName()).isEqualTo("Benalish Emissary");
    }

    @Test
    @DisplayName("Draws a card without destroying a lone target")
    void drawsWithoutAnotherPermanentWithSameName() {
        harness.addToBattlefield(player2, new BenalishLancer());
        harness.setLibrary(player1, List.of(new BenalishEmissary()));

        UUID targetId = harness.getPermanentId(player2, "Benalish Lancer");
        castWinnow(targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Benalish Lancer");
        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(card -> card.getName()).isEqualTo("Benalish Emissary");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");

        assertThatThrownBy(() -> castWinnow(targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWinnow(UUID targetId) {
        harness.setHand(player1, List.of(new Winnow()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, targetId);
    }
}
