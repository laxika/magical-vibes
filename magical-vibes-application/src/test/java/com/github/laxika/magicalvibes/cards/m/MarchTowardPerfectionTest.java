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
import com.github.laxika.magicalvibes.model.TurnStep;
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

        List<Permanent> myrs = findPermanents(player1, "Myr Convert");
        assertThat(myrs).hasSize(2);
        Permanent firstPermanent = myrs.getFirst();
        Permanent secondPermanent = myrs.getLast();

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

        Permanent myrPermanent = findPermanent(player1, "Myr Convert");
        assertThat(myrPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(myrPermanent.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
    }

    @Test
    void boonPersistsAcrossTurnsAndIgnoresOpponentsCreatureSpells() {
        harness.setHand(player1, List.of(new MarchTowardPerfection(), new MyrConvert()));
        harness.setHand(player2, List.of(new MyrConvert()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        Permanent opponentsMyr = findPermanent(player2, "Myr Convert");
        assertThat(opponentsMyr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentsMyr.getCounterCount(CounterType.DEATHTOUCH)).isZero();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent myr = findPermanent(player1, "Myr Convert");
        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(myr.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
    }

    @Test
    void multipleBoonsApplyToTheSameNextCreatureAndAreAllConsumed() {
        harness.setHand(player1, List.of(new MarchTowardPerfection(), new MarchTowardPerfection(),
                new MyrConvert(), new MyrConvert()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        for (int i = 0; i < 2; i++) {
            harness.castSorcery(player1, 0);
            harness.passBothPriorities();
            PendingInteraction.SpellbookDraftChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
            harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));
        }

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> myrs = findPermanents(player1, "Myr Convert");
        assertThat(myrs).hasSize(2);
        assertThat(myrs.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(myrs.getFirst().getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(2);
        assertThat(myrs.getLast().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(myrs.getLast().getCounterCount(CounterType.DEATHTOUCH)).isZero();
    }

    @Test
    void draftOffersDistinctSpellbookCardsAndPutsOnlyTheSelectedCardInHand() {
        harness.setHand(player1, List.of(new MarchTowardPerfection()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);
        assertThat(choice.cards().stream().map(Card::getName).toList())
                .doesNotHaveDuplicates()
                .isSubsetOf("Archfiend of the Dross", "Bilious Skulldweller", "Diminished Returner",
                        "Entomber Exarch", "Myr Convert", "Phyrexian Fleshgorger", "Phyrexian Gargantua",
                        "Phyrexian Obliterator", "Phyrexian Rager", "Phyrexian Revoker", "Scrapwork Rager",
                        "Soulless Jailer", "Toxic Abomination", "Vault Skirge", "Zenith Chronicler");

        Card drafted = choice.cards().getLast();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drafted);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
