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
        harness.passBothPriorities();
        harness.passBothPriorities();

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

    private List<Card> fiveGrizzlyBears() {
        return List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
    }
}
