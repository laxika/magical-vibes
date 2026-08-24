package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Despark.class, HillGiant.class, GrizzlyBears.class, Forest.class})
class DesparkTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target permanent with mana value 4 or greater")
    void exilesTargetPermanentWithManaValueAtLeastFour() {
        harness.addToBattlefield(player2, new HillGiant());
        castDespark(harness.getPermanentId(player2, "Hill Giant"));

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Cannot target a permanent with mana value less than 4")
    void cannotTargetPermanentWithManaValueLessThanFour() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> castDespark(targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> castDespark(targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDespark(UUID targetId) {
        harness.setHand(player1, List.of(new Despark()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
