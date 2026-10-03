package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SunshotMilitia;
import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.c.ConfoundingRiddle;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrasssTunnelGrinder.class, SunshotMilitia.class, Abrade.class,
        ConfoundingRiddle.class, Solemnity.class})
class BrasssTunnelGrinderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by discarding any number, then draws that many plus one")
    void entersWithDiscardAndDraw() {
        Card discarded = new SunshotMilitia();
        Card drawnFirst = new SunshotMilitia();
        Card drawnSecond = new SunshotMilitia();
        BrasssTunnelGrinder grinder = new BrasssTunnelGrinder();
        harness.setHand(player1, List.of(grinder, discarded));
        harness.setLibrary(player1, List.of(drawnFirst, drawnSecond));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnFirst, drawnSecond);
        harness.assertInGraveyard(player1, "Sunshot Militia");
    }

    @Test
    @DisplayName("Puts a bore counter at your end step after descending and transforms at three")
    void descendsAndTransformsAtThreeBoreCounters() {
        BrasssTunnelGrinder grinder = new BrasssTunnelGrinder();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, grinder);
        permanent.setCounterCount(CounterType.BORE, 2);
        gd.playersWhoDescendedThisTurn.add(player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(permanent.isTransformed()).isTrue();
        assertThat(permanent.getCounterCount(CounterType.BORE)).isZero();
    }

    @Test
    @DisplayName("Tecutlan discovers using the mana value of a permanent spell cast with its mana")
    void backFaceDiscoversForPermanentSpellManaValue() {
        Permanent rift = addTransformedGrinder();
        SunshotMilitia discovered = new SunshotMilitia();
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player1, List.of(new BrasssTunnelGrinder()));

        harness.activateAbility(player1, indexOf(player1, rift), 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
    }

    @Test
    void choosingZeroDiscardsStillDrawsOne() {
        Card kept = new SunshotMilitia();
        Card drawn = new SunshotMilitia();
        harness.setHand(player1, List.of(new BrasssTunnelGrinder(), kept));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyHandStillDrawsOne() {
        Card drawn = new SunshotMilitia();
        harness.setHand(player1, List.of(new BrasssTunnelGrinder()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void doesNotTriggerWithoutDescendingEvenWithThreeCounters() {
        Permanent grinder = harness.addToBattlefieldAndReturn(player1, new BrasssTunnelGrinder());
        grinder.setCounterCount(CounterType.BORE, 3);
        beginEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(grinder.isTransformed()).isFalse();
        assertThat(grinder.getCounterCount(CounterType.BORE)).isEqualTo(3);
    }

    @Test
    void descendingAddsOnlyOneCounterBelowThreshold() {
        Permanent grinder = harness.addToBattlefieldAndReturn(player1, new BrasssTunnelGrinder());
        gd.playersWhoDescendedThisTurn.add(player1.getId());
        beginEndStep(player1);
        resolveAllTriggers();

        assertThat(grinder.getCounterCount(CounterType.BORE)).isEqualTo(1);
        assertThat(grinder.isTransformed()).isFalse();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent grinder = harness.addToBattlefieldAndReturn(player1, new BrasssTunnelGrinder());
        gd.playersWhoDescendedThisTurn.add(player1.getId());
        beginEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(grinder.getCounterCount(CounterType.BORE)).isZero();
    }

    @Test
    @CardUsed(Solemnity.class)
    void transformsWithThreeExistingCountersEvenWhenAnotherCounterCannotBePlaced() {
        Permanent grinder = harness.addToBattlefieldAndReturn(player1, new BrasssTunnelGrinder());
        grinder.setCounterCount(CounterType.BORE, 3);
        harness.addToBattlefield(player2, new Solemnity());
        gd.playersWhoDescendedThisTurn.add(player1.getId());
        beginEndStep(player1);
        resolveAllTriggers();

        assertThat(grinder.isTransformed()).isTrue();
        assertThat(grinder.getCounterCount(CounterType.BORE)).isZero();
    }

    @Test
    void discoverKeepsManaValueWhenTriggeringSpellIsCountered() {
        Permanent rift = addTransformedGrinder();
        BrasssTunnelGrinder spell = new BrasssTunnelGrinder();
        Card discovered = new SunshotMilitia();
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of(new ConfoundingRiddle()));
        harness.activateAbility(player1, indexOf(player1, rift), 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{1}, spell.getId(), List.of());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    @Test
    void permanentSpellWithoutRiftManaDoesNotDiscover() {
        addTransformedGrinder();
        harness.setHand(player1, List.of(new SunshotMilitia()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void instantUsingRiftManaDoesNotDiscover() {
        Permanent rift = addTransformedGrinder();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SunshotMilitia());
        harness.setHand(player1, List.of(new Abrade()));
        harness.activateAbility(player1, indexOf(player1, rift), 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstantWithModes(player1, 0, 1, new int[]{0}, target.getId(), List.of());

        assertThat(gd.stack).hasSize(1);
    }

    private void beginEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addTransformedGrinder() {
        BrasssTunnelGrinder card = new BrasssTunnelGrinder();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

}
