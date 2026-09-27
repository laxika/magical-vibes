package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LucyMacLeanPositivelyArmed.class, RaiseTheAlarm.class, GrizzlyBears.class})
class LucyMacLeanPositivelyArmedTest extends BaseCardTest {

    @Test
    @DisplayName("A token entering under your control may be copied for an opponent and draws a card")
    void copiesOwnTokenForOpponentAndDraws() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addLucy(player1);

        createTokens(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(countTokens(player2)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's token may be copied for you without drawing")
    void copiesOpponentsTokenForControllerWithoutDrawing() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addLucy(player1);

        createTokens(player2);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPlayerIds()).containsExactly(player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(countTokens(player1)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger fires only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addLucy(player1);

        createTokens(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        createTokens(player1);
        harness.passBothPriorities();

        assertThat(countTokens(player2)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    private Permanent addLucy(Player player) {
        return harness.addToBattlefieldAndReturn(player, new LucyMacLeanPositivelyArmed());
    }

    private void createTokens(Player player) {
        List<Card> hand = new ArrayList<>(gd.playerHands.get(player.getId()));
        hand.add(new RaiseTheAlarm());
        harness.setHand(player, hand);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castInstant(player, hand.size() - 1);
        harness.passBothPriorities();
    }

    private long countTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }
}
