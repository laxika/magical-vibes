package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.h.HardHittingQuestion;
import com.github.laxika.magicalvibes.cards.n.NervousGardener;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MassacreGirlKnownKiller.class, NervousGardener.class, Shock.class, GloriousAnthem.class,
        HardHittingQuestion.class})
class MassacreGirlKnownKillerTest extends BaseCardTest {

    @Test
    @DisplayName("Massacre Girl grants wither to creatures you control")
    void grantsWitherToCreaturesYouControl() {
        Permanent killer = addCreatureReady(player1, new MassacreGirlKnownKiller());
        Permanent ownCreature = addCreatureReady(player1, new NervousGardener());
        Permanent opponentCreature = addCreatureReady(player2, new NervousGardener());

        assertThat(gqs.hasKeyword(gd, killer, Keyword.WITHER)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.WITHER)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.WITHER)).isFalse();
    }

    @Test
    @DisplayName("Draws when an opponent creature dies with toughness less than 1")
    void drawsWhenOpponentCreatureDiesWithToughnessLessThanOne() {
        harness.setHand(player1, List.of());
        NervousGardener drawnCard = new NervousGardener();
        harness.setLibrary(player1, List.of(drawnCard));

        addCreatureReady(player1, new MassacreGirlKnownKiller());
        Permanent attacker = addCreatureReady(player1, new NervousGardener());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new NervousGardener());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player2, "Nervous Gardener");
    }

    @Test
    @DisplayName("Does not draw when an opponent creature dies with toughness greater than 0")
    void doesNotDrawWhenOpponentCreatureDiesWithToughnessGreaterThanZero() {
        harness.setHand(player1, List.of(new Shock()));
        NervousGardener libraryCard = new NervousGardener();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.RED, 1);

        addCreatureReady(player1, new MassacreGirlKnownKiller());
        Permanent target = addCreatureReady(player2, new NervousGardener());

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertInGraveyard(player2, "Nervous Gardener");
    }

    @Test
    @DisplayName("Wither leaves counters on a surviving creature rather than marked damage")
    void witherLeavesCountersOnSurvivingCreature() {
        harness.setHand(player1, List.of());
        NervousGardener libraryCard = new NervousGardener();
        harness.setLibrary(player1, List.of(libraryCard));
        addCreatureReady(player1, new MassacreGirlKnownKiller());
        Permanent attacker = addCreatureReady(player1, new NervousGardener());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new NervousGardener());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Nervous Gardener");
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Does not draw when your own creature dies with zero toughness")
    void doesNotDrawForOwnCreatureWithZeroToughness() {
        harness.setHand(player1, List.of());
        NervousGardener libraryCard = new NervousGardener();
        harness.setLibrary(player1, List.of(libraryCard));
        addCreatureReady(player1, new MassacreGirlKnownKiller());
        Permanent ownCreature = addCreatureReady(player1, new NervousGardener());
        ownCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nervous Gardener");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Draws once for each opposing creature dying with nonpositive toughness")
    void drawsForEachOpposingCreatureWithNonpositiveToughness() {
        harness.setHand(player1, List.of());
        NervousGardener firstDraw = new NervousGardener();
        Shock secondDraw = new Shock();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        addCreatureReady(player1, new MassacreGirlKnownKiller());
        Permanent zeroToughness = addCreatureReady(player2, new NervousGardener());
        zeroToughness.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        Permanent negativeToughness = addCreatureReady(player2, new NervousGardener());
        negativeToughness.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        harness.assertNotOnBattlefield(player2, "Nervous Gardener");
    }

    @Test
    @DisplayName("Sees opposing creatures dying at the same time as Massacre Girl")
    void drawsWhenSourceDiesSimultaneously() {
        harness.setHand(player1, List.of());
        NervousGardener drawnCard = new NervousGardener();
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent killer = addCreatureReady(player1, new MassacreGirlKnownKiller());
        killer.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        Permanent opponentCreature = addCreatureReady(player2, new NervousGardener());
        opponentCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Massacre Girl, Known Killer");
        harness.assertInGraveyard(player2, "Nervous Gardener");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Uses toughness including a controller-dependent bonus before death")
    void doesNotDrawForPositiveLastKnownToughnessWithAnthem() {
        harness.setHand(player1, List.of(new Shock()));
        NervousGardener libraryCard = new NervousGardener();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.RED, 1);
        addCreatureReady(player1, new MassacreGirlKnownKiller());
        harness.addToBattlefield(player2, new GloriousAnthem());
        Permanent target = addCreatureReady(player2, new NervousGardener());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Nervous Gardener");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Wither applies to noncombat damage dealt by your creatures")
    void drawsAfterCreatureDealsNoncombatWitherDamage() {
        harness.setHand(player1, List.of(new HardHittingQuestion()));
        Shock drawnCard = new Shock();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        Permanent killer = addCreatureReady(player1, new MassacreGirlKnownKiller());
        Permanent target = addCreatureReady(player2, new NervousGardener());

        harness.castAndResolveSorcery(player1, 0, List.of(killer.getId(), target.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Nervous Gardener");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Wither deals normal damage to players")
    void witherDealsNormalDamageToPlayers() {
        harness.setLife(player2, 20);
        Permanent killer = addCreatureReady(player1, new MassacreGirlKnownKiller());
        killer.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Creatures lose granted wither when Massacre Girl leaves")
    void creaturesLoseWitherWhenSourceLeaves() {
        harness.setHand(player1, List.of());
        Permanent killer = addCreatureReady(player1, new MassacreGirlKnownKiller());
        Permanent ownCreature = addCreatureReady(player1, new NervousGardener());
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.WITHER)).isTrue();
        killer.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Massacre Girl, Known Killer");
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.WITHER)).isFalse();
    }
}
