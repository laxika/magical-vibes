package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GulfSquid;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.cards.w.WintermoonMesa;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JolraelEmpressOfBeasts.class, GulfSquid.class, RhysticCave.class, WintermoonMesa.class})
class JolraelEmpressOfBeastsTest extends BaseCardTest {

    @Test
    @DisplayName("Animates all lands controlled by the targeted player and discards two cards")
    void animatesTargetPlayersLands() {
        readyJolrael();
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new WintermoonMesa());
        Permanent targetOtherLand = harness.addToBattlefieldAndReturn(player2, new RhysticCave());
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new GulfSquid());

        activateAgainst(player2);

        assertThat(gqs.getEffectivePower(gd, targetLand)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, targetLand)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, targetLand)).isTrue();
        assertThat(gqs.isLand(gd, targetLand)).isTrue();
        assertThat(gqs.isCreature(gd, targetOtherLand)).isTrue();
        assertThat(gqs.isCreature(gd, ownLand)).isFalse();
        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, targetCreature)).isEqualTo(2);
        assertThat(findPermanent(player1, "Jolrael, Empress of Beasts").isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can target its controller's lands")
    void canTargetController() {
        readyJolrael();
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new RhysticCave());

        activateAgainst(player1);

        assertThat(gqs.isCreature(gd, ownLand)).isTrue();
        assertThat(gqs.isCreature(gd, opponentLand)).isFalse();
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOff() {
        readyJolrael();
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new WintermoonMesa());

        activateAgainst(player2);
        assertThat(gqs.isCreature(gd, targetLand)).isTrue();

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.isCreature(gd, targetLand)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a player")
    void cannotTargetPermanent() {
        readyJolrael();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RhysticCave());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without two cards to discard")
    void cannotActivateWithoutTwoCardsToDiscard() {
        readyJolrael();
        harness.setHand(player1, List.of(new GulfSquid()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void readyJolrael() {
        addCreatureReady(player1, new JolraelEmpressOfBeasts());
        harness.setHand(player1, List.of(new GulfSquid(), new GulfSquid()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void activateAgainst(com.github.laxika.magicalvibes.model.Player target) {
        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
