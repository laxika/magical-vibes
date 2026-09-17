package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Skulltap.class, ScornfulEgotist.class, TempleOfTheFalseGod.class})
class SkulltapTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature as an additional cost and draws two cards")
    void sacrificesCreatureAndDrawsTwoCards() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ScornfulEgotist());

        harness.setHand(player1, List.of(new Skulltap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Scornful Egotist");
        harness.assertInGraveyard(player1, "Scornful Egotist");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 2);
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        harness.setHand(player1, List.of(new Skulltap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature permanent")
    void cannotSacrificeNoncreaturePermanent() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new TempleOfTheFalseGod());

        harness.setHand(player1, List.of(new Skulltap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ScornfulEgotist());

        harness.setHand(player1, List.of(new Skulltap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }
}
