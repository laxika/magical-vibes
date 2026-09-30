package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArchfiendOfTheDross;
import com.github.laxika.magicalvibes.cards.b.BiliousSkulldweller;
import com.github.laxika.magicalvibes.cards.d.DiminishedReturner;
import com.github.laxika.magicalvibes.cards.e.EntomberExarch;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianFleshgorger;
import com.github.laxika.magicalvibes.cards.p.PhyrexianGargantua;
import com.github.laxika.magicalvibes.cards.p.PhyrexianObliterator;
import com.github.laxika.magicalvibes.cards.p.PhyrexianRager;
import com.github.laxika.magicalvibes.cards.p.PhyrexianRevoker;
import com.github.laxika.magicalvibes.cards.s.ScrapworkRager;
import com.github.laxika.magicalvibes.cards.s.SoullessJailer;
import com.github.laxika.magicalvibes.cards.t.ToxicAbomination;
import com.github.laxika.magicalvibes.cards.v.VaultSkirge;
import com.github.laxika.magicalvibes.cards.z.ZenithChronicler;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarchTowardPerfection.class, ArchfiendOfTheDross.class, BiliousSkulldweller.class,
        DiminishedReturner.class, EntomberExarch.class, MyrConvert.class,
        PhyrexianFleshgorger.class, PhyrexianGargantua.class, PhyrexianObliterator.class,
        PhyrexianRager.class, PhyrexianRevoker.class, ScrapworkRager.class,
        SoullessJailer.class, ToxicAbomination.class, VaultSkirge.class,
        ZenithChronicler.class, GrizzlyBears.class})
class MarchTowardPerfectionTest extends BaseCardTest {

    @Test
    void draftsThreeCardsFromItsSpellbook() {
        harness.setHand(player1, List.of(new MarchTowardPerfection()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);

        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
    }

    @Test
    void nextPhyrexianCreatureEntersWithPlusOneAndDeathtouchCountersOnce() {
        MarchTowardPerfection march = new MarchTowardPerfection();
        MyrConvert first = new MyrConvert();
        MyrConvert second = new MyrConvert();
        harness.setHand(player1, List.of(march, first, second));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent firstPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(first.getId()))
                .findFirst()
                .orElseThrow();
        Permanent secondPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(second.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(firstPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(firstPermanent.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, firstPermanent, Keyword.DEATHTOUCH)).isTrue();
        assertThat(secondPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(secondPermanent.getCounterCount(CounterType.DEATHTOUCH)).isZero();
    }

    @Test
    void nonPhyrexianCreatureDoesNotConsumeTheBoon() {
        MarchTowardPerfection march = new MarchTowardPerfection();
        GrizzlyBears bears = new GrizzlyBears();
        MyrConvert myr = new MyrConvert();
        harness.setHand(player1, List.of(march, bears, myr));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent myrPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(myr.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(myrPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(myrPermanent.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
    }
}
