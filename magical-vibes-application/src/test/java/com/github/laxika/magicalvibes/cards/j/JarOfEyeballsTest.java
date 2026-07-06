package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersAsCostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JarOfEyeballsTest extends BaseCardTest {

    // ===== Death trigger =====

    @Test
    @DisplayName("Puts two eyeball counters when an ally creature dies")
    void putsTwoCountersWhenAllyCreatureDies() {
        harness.addToBattlefield(player1, new JarOfEyeballs());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent jar = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Jar of Eyeballs"))
                .findFirst().orElseThrow();
        assertThat(jar.getCounterCount(CounterType.EYEBALL)).isZero();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger
        harness.passBothPriorities(); // Resolve Jar's eyeball counter trigger

        assertThat(jar.getCounterCount(CounterType.EYEBALL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does NOT get counters when an opponent's creature dies")
    void doesNotGetCountersWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new JarOfEyeballs());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent jar = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(jar.getCounterCount(CounterType.EYEBALL)).isZero();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die

        assertThat(jar.getCounterCount(CounterType.EYEBALL)).isZero();
    }

    @Test
    @DisplayName("Accumulates eyeball counters as multiple ally creatures die")
    void accumulatesCounters() {
        harness.addToBattlefield(player1, new JarOfEyeballs());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent jar = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Jar of Eyeballs"))
                .findFirst().orElseThrow();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(jar.getCounterCount(CounterType.EYEBALL)).isEqualTo(2);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bears2Id = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bears2Id);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(jar.getCounterCount(CounterType.EYEBALL)).isEqualTo(4);
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Activating removes all eyeball counters as a cost and enters hand/top/bottom choice")
    void activatingRemovesCountersAndEntersChoice() {
        Permanent jar = addReadyJar(player1);
        jar.setCounterCount(CounterType.EYEBALL, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        // Eyeball counters removed immediately as a cost
        assertThat(jar.getCounterCount(CounterType.EYEBALL)).isZero();

        harness.passBothPriorities(); // resolve ability from stack

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandTopBottomChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandTopBottomChoice.class).playerId()).isEqualTo(player1.getId());
        // X equals the number of eyeball counters removed this way
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandTopBottomChoice.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("Choosing a card puts it into hand and rest on bottom")
    void choosingCardPutsInHandRestOnBottom() {
        Permanent jar = addReadyJar(player1);
        jar.setCounterCount(CounterType.EYEBALL, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);
        Card top2 = deck.get(2);
        int originalDeckSize = deck.size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose: card 1 to hand, card 0 to top, card 2 to bottom
        gs.handleHandTopBottomChosen(gd, player1, 1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(top1);
        assertThat(deck.get(0)).isSameAs(top0);
        assertThat(deck.get(deck.size() - 1)).isSameAs(top2);
        assertThat(deck).hasSize(originalDeckSize - 1);
    }

    @Test
    @DisplayName("Activating with one eyeball counter auto-puts that card into hand")
    void oneCounterAutoChooses() {
        Permanent jar = addReadyJar(player1);
        jar.setCounterCount(CounterType.EYEBALL, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card topCard = deck.get(0);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Activated ability requires tap — tapped Jar cannot activate")
    void activatedAbilityRequiresTap() {
        Permanent jar = addReadyJar(player1);
        jar.setCounterCount(CounterType.EYEBALL, 2);
        jar.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    // ===== Helper methods =====

    private Permanent addReadyJar(Player player) {
        JarOfEyeballs card = new JarOfEyeballs();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
