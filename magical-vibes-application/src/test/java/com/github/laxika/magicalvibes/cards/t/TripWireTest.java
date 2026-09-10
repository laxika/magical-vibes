package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.cards.s.ShuDefender;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TripWire.class, ShuCavalry.class, ShuDefender.class})
class TripWireTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature with horsemanship")
    void destroysCreatureWithHorsemanship() {
        Permanent horseman = harness.addToBattlefieldAndReturn(player2, new ShuCavalry());
        harness.setHand(player1, List.of(new TripWire()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(horseman.getId()));

        assertThat(gqs.findPermanentById(harness.getGameData(), horseman.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(horseman.getCard());
    }

    @Test
    @DisplayName("Can target and destroy your own creature with horsemanship")
    void destroysOwnCreatureWithHorsemanship() {
        Permanent horseman = harness.addToBattlefieldAndReturn(player1, new ShuCavalry());
        harness.setHand(player1, List.of(new TripWire()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(horseman.getId()));

        assertThat(gqs.findPermanentById(harness.getGameData(), horseman.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(horseman.getCard());
    }

    @Test
    @DisplayName("Cannot target a creature without horsemanship")
    void cannotTargetCreatureWithoutHorsemanship() {
        Permanent defender = harness.addToBattlefieldAndReturn(player2, new ShuDefender());
        harness.setHand(player1, List.of(new TripWire()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(defender.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with horsemanship");
    }
}
