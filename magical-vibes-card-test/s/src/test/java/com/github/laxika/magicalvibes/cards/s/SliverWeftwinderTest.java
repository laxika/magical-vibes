package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BelligerentSliver;
import com.github.laxika.magicalvibes.cards.b.BladebackSliver;
import com.github.laxika.magicalvibes.cards.b.BlurSliver;
import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.c.CleavingSliver;
import com.github.laxika.magicalvibes.cards.c.CloudshredderSliver;
import com.github.laxika.magicalvibes.cards.d.DiffusionSliver;
import com.github.laxika.magicalvibes.cards.d.DregscapeSliver;
import com.github.laxika.magicalvibes.cards.e.EnduringSliver;
import com.github.laxika.magicalvibes.cards.f.FirstSliversChosen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HollowheadSliver;
import com.github.laxika.magicalvibes.cards.l.LancerSliver;
import com.github.laxika.magicalvibes.cards.l.LavabellySliver;
import com.github.laxika.magicalvibes.cards.l.LeechingSliver;
import com.github.laxika.magicalvibes.cards.m.ManaweftSliver;
import com.github.laxika.magicalvibes.cards.p.PredatorySliver;
import com.github.laxika.magicalvibes.cards.s.ScuttlingSliver;
import com.github.laxika.magicalvibes.cards.s.SentinelSliver;
import com.github.laxika.magicalvibes.cards.s.SliverHivelord;
import com.github.laxika.magicalvibes.cards.s.SpitefulSliver;
import com.github.laxika.magicalvibes.cards.s.SteelformSliver;
import com.github.laxika.magicalvibes.cards.s.StrikingSliver;
import com.github.laxika.magicalvibes.cards.t.TemperedSliver;
import com.github.laxika.magicalvibes.cards.t.TheFirstSliver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        SliverWeftwinder.class, BelligerentSliver.class, BladebackSliver.class, BlurSliver.class,
        BonescytheSliver.class, CleavingSliver.class, CloudshredderSliver.class, DiffusionSliver.class,
        DregscapeSliver.class, EnduringSliver.class, FirstSliversChosen.class, GrizzlyBears.class,
        HollowheadSliver.class, LancerSliver.class, LavabellySliver.class, LeechingSliver.class,
        ManaweftSliver.class, PredatorySliver.class, ScuttlingSliver.class, SentinelSliver.class,
        SliverHivelord.class, SpitefulSliver.class, SteelformSliver.class, StrikingSliver.class,
        TemperedSliver.class, TheFirstSliver.class})
class SliverWeftwinderTest extends BaseCardTest {

    @Test
    void sliverCardsInHandCanBeCastForWarpThree() {
        addCreatureReady(player1, new SliverWeftwinder());
        BelligerentSliver sliver = new BelligerentSliver();
        harness.setHand(player1, List.of(sliver));
        harness.setLibrary(player1, fiveGrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent warpedSliver = findPermanent(player1, "Belligerent Sliver");
        assertThat(warpedSliver.isCastWithWarp()).isTrue();
        assertThat(gd.spellWarpedThisTurn).isTrue();
    }

    @Test
    void ownSliverEntersAndConjuresFromSpellbookThenDraws() {
        addCreatureReady(player1, new SliverWeftwinder());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, fiveGrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new BelligerentSliver());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        List<Card> remainingCards = gd.playerDecks.get(player1.getId()).stream()
                .filter(card -> !(card instanceof GrizzlyBears))
                .toList();
        List<Card> drawnCards = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> !(card instanceof GrizzlyBears))
                .toList();
        assertThat(remainingCards.size() + drawnCards.size()).isEqualTo(1);
    }

    @Test
    void nonSliverEnteringDoesNotGetTheGrantedAbility() {
        addCreatureReady(player1, new SliverWeftwinder());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, fiveGrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void enteringWeftwinderGrantsItsOwnConjureTrigger() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, fiveGrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new SliverWeftwinder());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void opposingSliverDoesNotConjureOrDraw() {
        addCreatureReady(player1, new SliverWeftwinder());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, fiveGrizzlyBears());
        harness.setLibrary(player2, fiveGrizzlyBears());

        harness.enterBattlefieldAndReturn(player2, new BelligerentSliver());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void emptyLibraryReceivesConjuredCardBeforeDrawing() {
        addCreatureReady(player1, new SliverWeftwinder());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new BelligerentSliver());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getOwnerId())
                .isEqualTo(player1.getId());
    }

    @Test
    void conjuringIntoTopFivePreservesCardsBelowThatRange() {
        addCreatureReady(player1, new SliverWeftwinder());
        harness.setHand(player1, List.of());
        List<Card> originalLibrary = java.util.stream.Stream.generate(GrizzlyBears::new)
                .limit(10).map(card -> (Card) card).toList();
        harness.setLibrary(player1, originalLibrary);

        harness.enterBattlefieldAndReturn(player1, new BelligerentSliver());
        resolveAllTriggers();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(10);
        assertThat(library.subList(5, 10)).containsExactlyElementsOf(originalLibrary.subList(5, 10));
        List<Card> conjuredInLibrary = library.stream()
                .filter(card -> !(card instanceof GrizzlyBears)).toList();
        if (!conjuredInLibrary.isEmpty()) {
            assertThat(conjuredInLibrary).hasSize(1);
            assertThat(library.indexOf(conjuredInLibrary.getFirst())).isBetween(0, 3);
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(originalLibrary.getFirst());
        } else {
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            assertThat(gd.playerHands.get(player1.getId()).getFirst()).isNotInstanceOf(GrizzlyBears.class);
        }
    }

    @Test
    void nativeWarpWorksWithoutAnotherWeftwinderAndExilesAtEndStep() {
        SliverWeftwinder weftwinder = new SliverWeftwinder();
        harness.setHand(player1, List.of(weftwinder));
        harness.setLibrary(player1, fiveGrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Sliver Weftwinder").isCastWithWarp()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Sliver Weftwinder")).isZero();
        assertThat(gd.findExiledCard(weftwinder.getId())).isNotNull();
    }

    @Test
    void grantedTriggerStillResolvesAfterWeftwinderLeaves() {
        Permanent weftwinder = addCreatureReady(player1, new SliverWeftwinder());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, fiveGrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new BelligerentSliver());
        gd.playerBattlefields.get(player1.getId()).remove(weftwinder);
        gd.playerGraveyards.get(player1.getId()).add(weftwinder.getCard());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private List<Card> fiveGrizzlyBears() {
        return List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
    }
}
