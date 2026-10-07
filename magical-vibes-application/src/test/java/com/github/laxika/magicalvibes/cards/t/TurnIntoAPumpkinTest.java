package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RovingKeep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TurnIntoAPumpkin.class, RovingKeep.class, Island.class, Forest.class})
class TurnIntoAPumpkinTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a nonland permanent and draws a card")
    void returnsNonlandPermanentAndDraws() {
        harness.addToBattlefield(player2, new RovingKeep());
        UUID targetId = harness.getPermanentId(player2, "Roving Keep");
        harness.setHand(player1, List.of(new TurnIntoAPumpkin()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Roving Keep");
        harness.assertInHand(player2, "Roving Keep");
        harness.assertInHand(player1, "Island");
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Adamant creates a Food token after returning a nonland permanent and drawing")
    void adamantCreatesFood() {
        harness.addToBattlefield(player2, new RovingKeep());
        UUID targetId = harness.getPermanentId(player2, "Roving Keep");
        harness.setHand(player1, List.of(new TurnIntoAPumpkin()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new TurnIntoAPumpkin()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 4})
    @DisplayName("Adamant requires at least three blue mana actually spent")
    void adamantManaBoundary(int blueMana) {
        harness.addToBattlefield(player2, new RovingKeep());
        UUID targetId = harness.getPermanentId(player2, "Roving Keep");
        harness.setHand(player1, List.of(new TurnIntoAPumpkin()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, blueMana);
        if (blueMana < 4) {
            harness.addMana(player1, ManaColor.COLORLESS, 4 - blueMana);
        }

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Roving Keep");
        harness.assertInHand(player1, "Island");
        assertThat(countPermanents(player1, "Food")).isEqualTo(blueMana >= 3 ? 1L : 0L);
    }

    @Test
    @DisplayName("An illegal sole target prevents the draw and Food creation")
    void illegalTargetPreventsAllEffects() {
        harness.addToBattlefield(player2, new RovingKeep());
        UUID targetId = harness.getPermanentId(player2, "Roving Keep");
        harness.setHand(player1, List.of(new TurnIntoAPumpkin()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, targetId);
        harness.getPermanentRemovalService().removePermanentToHand(gd, findPermanent(player2, "Roving Keep"));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Turn into a Pumpkin");
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Can return your own permanent and immediately sacrifice the Food for life")
    void returnsOwnPermanentAndFoodGainsLife() {
        harness.addToBattlefield(player1, new RovingKeep());
        UUID targetId = harness.getPermanentId(player1, "Roving Keep");
        harness.setHand(player1, List.of(new TurnIntoAPumpkin()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Roving Keep");
        harness.assertNotOnBattlefield(player1, "Roving Keep");
        harness.assertInHand(player1, "Island");
        harness.assertOnBattlefield(player1, "Food");
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 3);
    }
}
