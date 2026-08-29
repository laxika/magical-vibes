package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaWakerOfTheDead;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GarruksHarbingerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player looks at that many cards and may put a creature into hand")
    void combatDamageToPlayerFindsCreature() {
        Card creature = new GrizzlyBears();
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

        Permanent planeswalker = new Permanent(new GarrukUnleashed());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player1.getId()).add(planeswalker);
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
        Permanent harbinger = new Permanent(new GarruksHarbinger());
        harbinger.setSummoningSick(false);
        harbinger.setAttacking(true);
        harbinger.setAttackTarget(targetId);
        gd.playerBattlefields.get(player1.getId()).add(harbinger);
        return harbinger;
    }

    private void stackTop(List<Card> topCards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        for (int i = topCards.size() - 1; i >= 0; i--) {
            deck.add(0, topCards.get(i));
        }
    }
}
