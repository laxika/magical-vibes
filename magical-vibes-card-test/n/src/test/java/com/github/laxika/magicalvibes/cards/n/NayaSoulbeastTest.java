package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DoubleMajor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NayaSoulbeast.class, AirElemental.class, Forest.class, GrizzlyBears.class,
        DoubleMajor.class, Humility.class})
class NayaSoulbeastTest extends BaseCardTest {

    @Test
    @DisplayName("Cast trigger reveals both top cards and uses their total mana value for counters")
    void entersWithCountersEqualToRevealedManaValues() {
        Card ownTopCard = new GrizzlyBears();
        Card opponentTopCard = new AirElemental();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opponentTopCard));
        harness.setHand(player1, List.of(new NayaSoulbeast()));
        addManaToCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(ownTopCard);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(opponentTopCard);

        harness.passBothPriorities();

        Permanent soulbeast = findPermanent(player1, "Naya Soulbeast");
        assertThat(soulbeast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, soulbeast)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, soulbeast)).isEqualTo(7);
    }

    @Test
    @DisplayName("An empty library contributes no counters and a zero-power Soulbeast dies")
    void emptyLibrariesContributeZero() {
        Card ownTopCard = new Forest();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new NayaSoulbeast()));
        addManaToCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(ownTopCard);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Naya Soulbeast");
        harness.assertInGraveyard(player1, "Naya Soulbeast");
    }

    @Test
    @DisplayName("The cast trigger reveals the cards on top when it resolves")
    void revealsCardsAtTriggerResolution() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new NayaSoulbeast()));
        addManaToCast();

        harness.castCreature(player1, 0);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new AirElemental()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent soulbeast = findPermanent(player1, "Naya Soulbeast");
        assertThat(soulbeast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("Changing library tops after the cast trigger resolves does not change entry counters")
    void countersUseResolvedTriggerSnapshot() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new AirElemental()));
        harness.setHand(player1, List.of(new NayaSoulbeast()));
        addManaToCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of());
        harness.passBothPriorities();

        Permanent soulbeast = findPermanent(player1, "Naya Soulbeast");
        assertThat(soulbeast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("Entering without being cast does not grant entry counters")
    void enteringWithoutCastingHasNoCounters() {
        Card ownTopCard = new GrizzlyBears();
        Card opponentTopCard = new AirElemental();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opponentTopCard));

        harness.enterBattlefieldAndReturn(player1, new NayaSoulbeast());
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTopCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTopCard);
        harness.assertNotOnBattlefield(player1, "Naya Soulbeast");
        harness.assertInGraveyard(player1, "Naya Soulbeast");
    }

    @Test
    @DisplayName("Humility does not remove entry counters established by the cast trigger")
    void resolvedCastTriggerCountersSurviveHumility() {
        harness.addToBattlefield(player2, new Humility());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new AirElemental()));
        harness.setHand(player1, List.of(new NayaSoulbeast()));
        addManaToCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent soulbeast = findPermanent(player1, "Naya Soulbeast");
        assertThat(soulbeast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, soulbeast)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, soulbeast)).isEqualTo(8);
    }

    @Test
    @DisplayName("A spell copy does not inherit the original spell's resolved cast-trigger entry effect")
    void spellCopyAfterCastTriggerDoesNotInheritCounters() {
        NayaSoulbeast soulbeast = new NayaSoulbeast();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new AirElemental()));
        harness.setHand(player1, List.of(soulbeast, new DoubleMajor()));
        addManaToCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, soulbeast.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent original = findPermanent(player1, "Naya Soulbeast");
        assertThat(original.getCard().isToken()).isFalse();
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    private void addManaToCast() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
