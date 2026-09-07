package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(KraulWhipcracker.class)
class KraulWhipcrackerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys a token an opponent controls")
    void etbDestroysOpponentsToken() {
        Permanent token = addToken(player2, true);
        castWhipcracker(List.of(token.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Soldier");
        harness.assertOnBattlefield(player1, "Kraul Whipcracker");
    }

    @Test
    @DisplayName("ETB cannot target a token you control")
    void etbCannotTargetOwnToken() {
        Permanent token = addToken(player1, true);

        assertThatThrownBy(() -> castWhipcracker(List.of(token.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a token an opponent controls");
    }

    @Test
    @DisplayName("ETB cannot target a nontoken permanent")
    void etbCannotTargetNontokenPermanent() {
        Permanent creature = addToken(player2, false);

        assertThatThrownBy(() -> castWhipcracker(List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a token an opponent controls");
    }

    private void castWhipcracker(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new KraulWhipcracker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0, targetIds);
    }

    private Permanent addToken(com.github.laxika.magicalvibes.model.Player player, boolean token) {
        Card card = new Card();
        card.setName("Soldier");
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(token);
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
