package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.l.LilianaWakerOfTheDead;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GarruksHarbinger.class, AlpineWatchdog.class, Forest.class, Shock.class,
        GarrukUnleashed.class, LilianaWakerOfTheDead.class, GraspOfDarkness.class})
class GarruksHarbingerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player looks at that many cards and may put a creature into hand")
    void combatDamageToPlayerFindsCreature() {
        Card creature = new AlpineWatchdog();
        stackTop(List.of(new Forest(), new Shock(), creature, new Forest()));
        addAttacker(player2.getId());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Combat damage to a planeswalker also triggers the ability")
    void combatDamageToPlaneswalkerTriggers() {
        Card garruk = new GarrukUnleashed();
        stackTop(List.of(new Forest(), new Shock(), garruk, new Forest()));

        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukUnleashed());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        addAttacker(planeswalker.getId());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(garruk.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(garruk);
    }

    @Test
    @DisplayName("Only creatures and Garruk planeswalker cards are eligible")
    void ignoresOtherPlaneswalkerCards() {
        List<Card> top = List.of(new LilianaWakerOfTheDead(), new Shock(), new Forest(), new Shock());
        stackTop(top);
        addAttacker(player2.getId());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContainAnyElementsOf(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsAll(top);
    }

    private Permanent addAttacker(java.util.UUID targetId) {
        Permanent harbinger = harness.addToBattlefieldAndReturn(player1, new GarruksHarbinger());
        harbinger.setSummoningSick(false);
        harbinger.setAttacking(true);
        harbinger.setAttackTarget(targetId);
        return harbinger;
    }

    private void stackTop(List<Card> topCards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.addAll(0, topCards);
    }

    @Test
    void mayDeclineCreatureAndBottomAllLookedAtCards() {
        List<Card> top = List.of(new AlpineWatchdog(), new Forest(), new Shock(), new Forest());
        Card untouched = new LilianaWakerOfTheDead();
        harness.setLibrary(player1, List.of(top.get(0), top.get(1), top.get(2), top.get(3), untouched));
        addAttacker(player2.getId());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContainAnyElementsOf(top);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5).startsWith(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrderElementsOf(top);
    }

    @Test
    void shortLibraryStillAllowsChoosingCreature() {
        Card creature = new AlpineWatchdog();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));
        addAttacker(player2.getId());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void opponentRedSpellCanTargetHarbinger() {
        Permanent harbinger = harness.addToBattlefieldAndReturn(player2, new GarruksHarbinger());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harbinger.getId());
        harness.passBothPriorities();

        assertThat(harbinger.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void opponentBlackSpellCannotTargetHarbinger() {
        Permanent harbinger = harness.addToBattlefieldAndReturn(player1, new GarruksHarbinger());
        harness.addToBattlefield(player1, new AlpineWatchdog());
        harness.setHand(player2, List.of(new GraspOfDarkness()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, harbinger.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void controllerBlackSpellCanTargetHarbinger() {
        harness.addToBattlefield(player1, new GarruksHarbinger());
        harness.setHand(player1, List.of(new GraspOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Garruk's Harbinger"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Garruk's Harbinger");
    }

    @Test
    void emptyLibraryDoesNotCreateAChoiceOrCauseADrawLoss() {
        harness.setLibrary(player1, List.of());
        addAttacker(player2.getId());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 16);
    }

    @Test
    void onlyLooksAtCardsEqualToDamageAndBottomsUnchosenCards() {
        Card chosen = new AlpineWatchdog();
        Card unchosen = new GarrukUnleashed();
        Card land = new Forest();
        Card spell = new Shock();
        Card untouched = new AlpineWatchdog();
        harness.setLibrary(player1, List.of(chosen, unchosen, land, spell, untouched));
        addAttacker(player2.getId());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen).doesNotContain(unchosen, untouched);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).startsWith(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(unchosen, land, spell);
    }
}
