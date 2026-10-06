package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Griselbrand.class, Forest.class})
class GriselbrandTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 7 life draws seven cards")
    void payingSevenLifeDrawsSevenCards() {
        harness.addToBattlefield(player1, new Griselbrand());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, forests(7));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Can activate repeatedly, paying 7 life each time")
    void canActivateRepeatedly() {
        harness.addToBattlefield(player1, new Griselbrand());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, forests(14));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(14);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot activate with less than 7 life")
    void cannotActivateWithInsufficientLife() {
        harness.addToBattlefield(player1, new Griselbrand());
        harness.setLibrary(player1, forests(7));
        harness.setLife(player1, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Life is paid immediately but cards are drawn only on resolution")
    void paysLifeBeforeDrawing() {
        harness.addToBattlefield(player1, new Griselbrand());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, forests(7));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Griselbrand can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        var griselbrand = harness.addToBattlefieldAndReturn(player1, new Griselbrand());
        griselbrand.tap();
        griselbrand.setSummoningSick(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, forests(7));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(griselbrand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability resolves after Griselbrand leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        var griselbrand = harness.addToBattlefieldAndReturn(player1, new Griselbrand());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, forests(7));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(griselbrand);
        harness.setGraveyard(player1, List.of(griselbrand.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private List<Card> forests(int count) {
        List<Card> cards = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        return cards;
    }

}
