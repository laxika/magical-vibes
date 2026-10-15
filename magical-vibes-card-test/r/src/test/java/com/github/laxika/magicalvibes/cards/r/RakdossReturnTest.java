package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.j.JaceArchitectOfThought;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AxebaneGuardian;
import com.github.laxika.magicalvibes.cards.a.AzoriusCharm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakdossReturn.class, AxebaneGuardian.class, AzoriusCharm.class, Forest.class, JaceArchitectOfThought.class})
class RakdossReturnTest extends BaseCardTest {

    // "Rakdos's Return deals X damage to target opponent or planeswalker. That player or that
    //  planeswalker's controller discards X cards."

    private void giveReturn(int x) {
        harness.setHand(player1, List.of(new RakdossReturn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        if (x > 0) {
            harness.addMana(player1, ManaColor.COLORLESS, x);
        }
    }

    @Test
    @DisplayName("Deals X damage to the targeted opponent and makes them discard X cards")
    void damageAndDiscardToTargetOpponent() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new AxebaneGuardian(), new AzoriusCharm(), new Forest(), new AxebaneGuardian())));
        giveReturn(3);
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(3);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("X=0 deals no damage and discards nothing")
    void xZeroDoesNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new AxebaneGuardian(), new AzoriusCharm())));
        giveReturn(0);
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Empty-handed opponent still takes X damage with no discard")
    void emptyHandTargetStillTakesDamage() {
        harness.setHand(player2, new ArrayList<>(List.of()));
        giveReturn(2);
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Targeting a planeswalker removes X loyalty and its controller discards X cards")
    void damageAndDiscardToPlaneswalkerController() {
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceArchitectOfThought());
        jace.setCounterCount(CounterType.LOYALTY, 5);

        harness.setHand(player2, new ArrayList<>(List.of(new AxebaneGuardian(), new AzoriusCharm(), new Forest())));
        giveReturn(2);

        harness.castAndResolveSorcery(player1, 0, 2, jace.getId());

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        giveReturn(1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new AxebaneGuardian());
        giveReturn(1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1,
                harness.getPermanentId(player2, "Axebane Guardian")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target your own planeswalker and makes you discard")
    void canTargetOwnPlaneswalker() {
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceArchitectOfThought());
        jace.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player2, List.of(new Forest()));
        giveReturn(2);
        gd.playerHands.get(player1.getId()).addAll(List.of(new AxebaneGuardian(), new AzoriusCharm()));

        harness.castAndResolveSorcery(player1, 0, 2, jace.getId());

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Lethal planeswalker damage still makes its controller discard")
    void lethalPlaneswalkerDamageStillDiscards() {
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceArchitectOfThought());
        jace.setCounterCount(CounterType.LOYALTY, 2);
        harness.setHand(player2, List.of(new AxebaneGuardian(), new AzoriusCharm(), new Forest()));
        giveReturn(3);

        harness.castAndResolveSorcery(player1, 0, 3, jace.getId());

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Jace, Architect of Thought");
        harness.assertNotOnBattlefield(player2, "Jace, Architect of Thought");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opponent with fewer than X cards discards their whole hand and takes full damage")
    void fewerCardsThanX() {
        harness.setHand(player2, List.of(new AxebaneGuardian(), new Forest()));
        giveReturn(4);

        harness.castAndResolveSorcery(player1, 0, 4, player2.getId());
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Preventing planeswalker damage does not reduce the number of cards discarded")
    void preventedDamageStillDiscardsXCards() {
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceArchitectOfThought());
        jace.setCounterCount(CounterType.LOYALTY, 4);
        jace.setDamagePreventionShield(3);
        harness.setHand(player2, List.of(new AxebaneGuardian(), new AzoriusCharm(), new Forest()));
        giveReturn(3);

        harness.castAndResolveSorcery(player1, 0, 3, jace.getId());

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(3);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A planeswalker leaving before resolution makes the spell do nothing")
    void departedPlaneswalkerDoesNotCauseDiscard() {
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceArchitectOfThought());
        jace.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player2, List.of(new AxebaneGuardian(), new Forest()));
        giveReturn(2);
        harness.castSorcery(player1, 0, 2, jace.getId());
        gd.playerBattlefields.get(player2.getId()).remove(jace);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Rakdos's Return");
    }
}
