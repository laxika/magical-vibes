package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragToTheUnderworld.class, BlackKnight.class, Forest.class, GrizzlyBears.class})
class DragToTheUnderworldTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature")
    void destroysTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragToTheUnderworld()));
        addFullMana();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Costs two less for two black mana symbols among permanents you control")
    void costsTwoLessForBlackDevotion() {
        harness.addToBattlefield(player1, new BlackKnight());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragToTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not count an opponent's black devotion")
    void doesNotCountOpponentsBlackDevotion() {
        harness.addToBattlefield(player2, new BlackKnight());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragToTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DragToTheUnderworld()));
        addFullMana();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addFullMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
