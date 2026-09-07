package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaptiveWeird.class, CompleatedConjurer.class})
class CaptiveWeirdTest extends BaseCardTest {

    @Test
    void transformsAndExilesTopCardWithPlayPermission() {
        Card topCard = putOnTopOfLibrary(player1);
        Permanent captive = addCaptive();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(captive.isTransformed()).isTrue();
        assertThat(captive.getCard()).isInstanceOf(CompleatedConjurer.class);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(topCard.getId());
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    void canPayPhyrexianManaWithLife() {
        Permanent captive = addCaptive();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(captive.isTransformed()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void canOnlyTransformAtSorcerySpeed() {
        addCaptive();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addCaptive() {
        return harness.addToBattlefieldAndReturn(player1, new CaptiveWeird());
    }

    private Card putOnTopOfLibrary(Player player) {
        Card card = new Card();
        gd.playerDecks.get(player.getId()).addFirst(card);
        return card;
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
