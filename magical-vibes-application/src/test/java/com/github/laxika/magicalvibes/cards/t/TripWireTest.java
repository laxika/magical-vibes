package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TripWireTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature with horsemanship")
    void destroysCreatureWithHorsemanship() {
        harness.addToBattlefield(player2, new ShuCavalry());
        harness.setHand(player1, List.of(new TripWire()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID horsemanId = gd.playerBattlefields.get(player2.getId()).get(0).getId();

        harness.castSorcery(player1, 0, List.of(horsemanId));
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(harness.getGameData(), horsemanId)).isNull();
    }

    @Test
    @DisplayName("Cannot target a creature without horsemanship")
    void cannotTargetCreatureWithoutHorsemanship() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TripWire()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearsId = gd.playerBattlefields.get(player2.getId()).get(0).getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }
}
