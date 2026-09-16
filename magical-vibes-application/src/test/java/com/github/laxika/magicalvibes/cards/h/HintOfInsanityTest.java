package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.ObstinateBaloth;
import com.github.laxika.magicalvibes.cards.p.PsychicPurge;
import com.github.laxika.magicalvibes.cards.r.RielleTheEverwise;
import com.github.laxika.magicalvibes.cards.t.TamiyoCollectorOfTales;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HintOfInsanity.class, AvenFlock.class, DuskImp.class, Forest.class})
class HintOfInsanityTest extends BaseCardTest {

    @Test
    @DisplayName("Discards all duplicate nonland cards and leaves unique cards and lands")
    void discardsDuplicateNonlandCards() {
        harness.setHand(player2, List.of(new DuskImp(), new DuskImp(), new Forest(), new AvenFlock()));
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Aven Flock");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Dusk Imp", "Dusk Imp");
    }

    @Test
    @DisplayName("A hand without duplicate nonland names is unchanged")
    void noDuplicateNonlandNamesDoesNothing() {
        harness.setHand(player2, List.of(new DuskImp(), new Forest(), new AvenFlock()));
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Dusk Imp", "Forest", "Aven Flock");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Duplicate lands remain in hand while duplicate nonlands are discarded")
    void duplicateLandsRemainInHand() {
        harness.setHand(player2, List.of(
                new DuskImp(), new DuskImp(), new Forest(), new Forest(), new AvenFlock()));
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Forest", "Aven Flock");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Dusk Imp", "Dusk Imp");
    }

    @Test
    @CardUsed(RielleTheEverwise.class)
    @DisplayName("Counts every card discarded in one discard event")
    void countsEveryCardDiscardedInOneEvent() {
        harness.addToBattlefield(player2, new RielleTheEverwise());
        harness.setHand(player2, List.of(new DuskImp(), new DuskImp(), new Forest()));
        harness.setLibrary(player2, List.of(new AvenFlock(), new AvenFlock()));
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Aven Flock", "Aven Flock");
    }

    @Test
    @CardUsed(TamiyoCollectorOfTales.class)
    @DisplayName("An opponent's discard-prevention effect stops the discard")
    void opponentDiscardPreventionStopsTheDiscard() {
        harness.addToBattlefield(player2, new TamiyoCollectorOfTales());
        harness.setHand(player2, List.of(new DuskImp(), new DuskImp(), new Forest()));
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Dusk Imp", "Dusk Imp", "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed(ObstinateBaloth.class)
    @DisplayName("A duplicate creature with a discard replacement enters the battlefield")
    void discardReplacementPutsMatchingCreatureOntoBattlefield() {
        harness.setHand(player2, List.of(new ObstinateBaloth(), new ObstinateBaloth(), new Forest()));
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Obstinate Baloth", "Obstinate Baloth");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed(PsychicPurge.class)
    @DisplayName("Discarding your own duplicate cards does not trigger opponent-discard abilities")
    void selfTargetDoesNotCountAsOpponentCausedDiscard() {
        harness.setHand(player1, List.of(
                new HintOfInsanity(), new PsychicPurge(), new PsychicPurge(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
    }
}
