package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NezumiShortfangTest extends BaseCardTest {

    @Test
    @DisplayName("Ability makes the target opponent discard a card of their choice")
    void abilityMakesTargetOpponentDiscard() {
        Permanent shortfang = addShortfang(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        activateShortfang(shortfang);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Emptying the target opponent's hand flips this into Stabwhisker the Odious")
    void flipsWhenTargetOpponentHandBecomesEmpty() {
        Permanent shortfang = addShortfang(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        activateShortfang(shortfang);
        harness.handleCardChosen(player2, 0);

        assertThat(shortfang.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not flip while the target opponent still holds a card")
    void doesNotFlipWhenTargetOpponentStillHasCards() {
        Permanent shortfang = addShortfang(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        activateShortfang(shortfang);
        harness.handleCardChosen(player2, 0);

        assertThat(shortfang.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("An already-empty hand flips this without any discard")
    void flipsWhenTargetOpponentHandAlreadyEmpty() {
        Permanent shortfang = addShortfang(player1);
        harness.setHand(player2, new ArrayList<>());

        activateShortfang(shortfang);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(shortfang.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target its own controller")
    void cannotTargetOwnController() {
        Permanent shortfang = addShortfang(player1);
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, shortfang), null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(shortfang.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Flipped: opponent with one card in hand loses 2 life at their upkeep")
    void flippedDrainsForEachCardFewerThanThree() {
        addFlippedShortfang(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Flipped: a hand of three or more cards costs no life")
    void flippedDoesNothingAtThreeOrMoreCards() {
        addFlippedShortfang(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears())));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Flipped: the controller's own upkeep never triggers")
    void flippedDoesNotTriggerOnControllerUpkeep() {
        addFlippedShortfang(player1);
        harness.setHand(player1, new ArrayList<>());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private Permanent addShortfang(Player player) {
        return addCreatureReady(player, new NezumiShortfang());
    }

    private Permanent addFlippedShortfang(Player player) {
        NezumiShortfang card = new NezumiShortfang();
        Permanent perm = addCreatureReady(player, card);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }

    private void activateShortfang(Permanent shortfang) {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, indexOf(player1, shortfang), null, player2.getId());
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
