package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.a.AvenWarhawk;
import com.github.laxika.magicalvibes.cards.g.GempalmPolluter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZombieBrute.class, GempalmPolluter.class, AvenWarhawk.class})
class ZombieBruteTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one counter for each revealed Zombie card")
    void entersWithCounterForEachRevealedZombieCard() {
        ZombieBrute card = new ZombieBrute();
        GempalmPolluter firstZombie = new GempalmPolluter();
        GempalmPolluter secondZombie = new GempalmPolluter();
        harness.setHand(player1, List.of(
                card, firstZombie, secondZombie, new AvenWarhawk()));
        payMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstZombie.getId(), secondZombie.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstZombie.getId(), secondZombie.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Zombie Brute")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The controller may reveal only some of the Zombie cards")
    void choosesSubsetOfZombieCards() {
        ZombieBrute card = new ZombieBrute();
        GempalmPolluter firstZombie = new GempalmPolluter();
        GempalmPolluter secondZombie = new GempalmPolluter();
        harness.setHand(player1, List.of(card, firstZombie, secondZombie));
        payMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(firstZombie.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Zombie Brute")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts only revealed Zombie cards from its controller's hand")
    void ignoresOtherHandsAndNonmatchingCards() {
        ZombieBrute card = new ZombieBrute();
        GempalmPolluter ownZombie = new GempalmPolluter();
        harness.setHand(player1, List.of(card, ownZombie, new AvenWarhawk()));
        harness.setHand(player2, List.of(new GempalmPolluter()));
        payMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownZombie.getId());

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Zombie Brute")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new ZombieBrute());
        Permanent blocker = addCreatureReady(player2, new GempalmPolluter());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3,
                player2.getId(), 2
        ));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
    }

    private void payMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
