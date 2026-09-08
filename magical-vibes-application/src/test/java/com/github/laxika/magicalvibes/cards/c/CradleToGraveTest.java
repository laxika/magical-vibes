package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CradleToGrave.class, GrizzlyBears.class, MassOfGhouls.class})
class CradleToGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature that entered the battlefield this turn")
    void destroysCreatureThatEnteredThisTurn() {
        Card creature = new GrizzlyBears();
        addCreatureToBattlefieldThisTurn(creature);

        castCradleToGrave(creature);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Card creature = new MassOfGhouls();
        addCreatureToBattlefieldThisTurn(creature);

        assertThatThrownBy(() -> castCradleToGrave(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a creature that did not enter the battlefield this turn")
    void cannotTargetOlderCreature() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player2, creature);

        assertThatThrownBy(() -> castCradleToGrave(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered the battlefield this turn");
    }

    private void addCreatureToBattlefieldThisTurn(Card creature) {
        harness.addToBattlefield(player2, creature);
        Map<UUID, List<Card>> enteredThisTurn = gd.permanentsEnteredBattlefieldThisTurn;
        enteredThisTurn.put(player2.getId(), new ArrayList<>(List.of(creature)));
    }

    private void castCradleToGrave(Card creature) {
        harness.setHand(player1, List.of(new CradleToGrave()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, creature.getName()));
        harness.passBothPriorities();
    }
}
