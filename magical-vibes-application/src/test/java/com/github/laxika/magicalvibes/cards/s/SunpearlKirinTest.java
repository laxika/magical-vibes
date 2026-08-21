package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunpearlKirin.class, GrizzlyBears.class, Island.class})
class SunpearlKirinTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a nontoken permanent without drawing a card")
    void returnsNontokenPermanentWithoutDrawing() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island()));
        castKirin(bears.getId());

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card instanceof Island);
    }

    @Test
    @DisplayName("Returns a token and draws a card")
    void returnsTokenAndDrawsCard() {
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCreature());
        harness.setLibrary(player1, List.of(new Island()));
        castKirin(token.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Island);
    }

    @Test
    @DisplayName("Cannot target an opponent's permanent")
    void cannotTargetOpponentsPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SunpearlKirin()));
        addManaForKirin();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nonland permanent you control");
    }

    private void castKirin(UUID targetId) {
        harness.setHand(player1, List.of(new SunpearlKirin()));
        addManaForKirin();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addManaForKirin() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private static Card tokenCreature() {
        Card token = new Card();
        token.setName("Spirit");
        token.setType(CardType.CREATURE);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        return token;
    }
}
