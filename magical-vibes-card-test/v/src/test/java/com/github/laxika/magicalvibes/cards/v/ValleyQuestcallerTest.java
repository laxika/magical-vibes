package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BeckCall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntrepidRabbit;
import com.github.laxika.magicalvibes.cards.l.LifecreedDuo;
import com.github.laxika.magicalvibes.cards.n.NettleGuard;
import com.github.laxika.magicalvibes.cards.s.ShrikeForce;
import com.github.laxika.magicalvibes.cards.s.StarscapeCleric;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValleyQuestcaller.class, BeckCall.class, GrizzlyBears.class, IntrepidRabbit.class,
        LifecreedDuo.class, NettleGuard.class, ShrikeForce.class, StarscapeCleric.class, Xenograft.class})
class ValleyQuestcallerTest extends BaseCardTest {

    @Test
    void buffsOtherControlledRabbitsAndNotOtherCreaturesOrOpponents() {
        Permanent rabbit = addCreatureReady(player1, new IntrepidRabbit());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentRabbit = addCreatureReady(player2, new IntrepidRabbit());

        int rabbitPower = gqs.getEffectivePower(gd, rabbit);
        int rabbitToughness = gqs.getEffectiveToughness(gd, rabbit);
        int bearsPower = gqs.getEffectivePower(gd, bears);
        int bearsToughness = gqs.getEffectiveToughness(gd, bears);
        int opponentRabbitPower = gqs.getEffectivePower(gd, opponentRabbit);
        int opponentRabbitToughness = gqs.getEffectiveToughness(gd, opponentRabbit);
        addCreatureReady(player1, new ValleyQuestcaller());

        assertThat(gqs.getEffectivePower(gd, rabbit)).isEqualTo(rabbitPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, rabbit)).isEqualTo(rabbitToughness + 1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(bearsPower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(bearsToughness);
        assertThat(gqs.getEffectivePower(gd, opponentRabbit)).isEqualTo(opponentRabbitPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentRabbit)).isEqualTo(opponentRabbitToughness);
    }

    @Test
    void scriesOnceWhenSeveralMatchingCreaturesEnterTogether() {
        Permanent questcaller = addCreatureReady(player1, new ValleyQuestcaller());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BeckCall()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.stack)
                .filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getId().equals(questcaller.getCard().getId()))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(4)
                .allSatisfy(bird -> {
                    assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);
                });

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void buffsBatsBirdsAndMiceOnlyOnceAndExcludesItself() {
        Permanent duo = addCreatureReady(player1, new LifecreedDuo());
        Permanent bird = addCreatureReady(player1, new ShrikeForce());
        Permanent mouse = addCreatureReady(player1, new NettleGuard());
        Permanent bat = addCreatureReady(player1, new StarscapeCleric());
        List<Permanent> creatures = List.of(duo, bird, mouse, bat);
        List<Integer> powers = creatures.stream().map(p -> gqs.getEffectivePower(gd, p)).toList();
        List<Integer> toughnesses = creatures.stream().map(p -> gqs.getEffectiveToughness(gd, p)).toList();
        ValleyQuestcaller card = new ValleyQuestcaller();
        Permanent questcaller = new Permanent(card);
        int ownPower = gqs.getEffectivePower(gd, questcaller);
        int ownToughness = gqs.getEffectiveToughness(gd, questcaller);
        gd.playerBattlefields.get(player1.getId()).add(questcaller);

        assertThat(gqs.getEffectivePower(gd, questcaller)).isEqualTo(ownPower);
        assertThat(gqs.getEffectiveToughness(gd, questcaller)).isEqualTo(ownToughness);
        for (int i = 0; i < creatures.size(); i++) {
            assertThat(gqs.getEffectivePower(gd, creatures.get(i))).isEqualTo(powers.get(i) + 1);
            assertThat(gqs.getEffectiveToughness(gd, creatures.get(i))).isEqualTo(toughnesses.get(i) + 1);
        }
        gd.playerBattlefields.get(player1.getId()).remove(questcaller);
        for (int i = 0; i < creatures.size(); i++) {
            assertThat(gqs.getEffectivePower(gd, creatures.get(i))).isEqualTo(powers.get(i));
            assertThat(gqs.getEffectiveToughness(gd, creatures.get(i))).isEqualTo(toughnesses.get(i));
        }
    }

    @Test
    void doesNotScryForItsOwnEntry() {
        harness.setHand(player1, List.of(new ValleyQuestcaller()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotScryForNonmatchingCreatureEntry() {
        addCreatureReady(player1, new ValleyQuestcaller());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotScryForOpponentsMatchingCreatureEntry() {
        addCreatureReady(player1, new ValleyQuestcaller());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new NettleGuard()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scriesOnceForCreatureWithBothBatAndBirdTypes() {
        addCreatureReady(player1, new ValleyQuestcaller());
        assertEntryScriesOnce(new LifecreedDuo(), 1);
    }

    @Test
    void scriesForBirdEntry() {
        addCreatureReady(player1, new ValleyQuestcaller());
        assertEntryScriesOnce(new ShrikeForce(), 2);
    }

    @Test
    void scriesForEachSeparateMouseEntryInTheSameTurn() {
        addCreatureReady(player1, new ValleyQuestcaller());
        assertEntryScriesOnce(new NettleGuard(), 1);
        assertEntryScriesOnce(new NettleGuard(), 1);
    }

    @Test
    void scriesForAnotherQuestcallerEntry() {
        addCreatureReady(player1, new ValleyQuestcaller());
        assertEntryScriesOnce(new ValleyQuestcaller(), 1);
    }

    private void assertEntryScriesOnce(Card card, int genericMana) {
        assertEntryScriesOnce(card, genericMana, ManaColor.WHITE);
    }

    private void assertEntryScriesOnce(Card card, int genericMana, ManaColor color) {
        harness.setLibrary(player1, List.of(new ShrikeForce()));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, color, 1);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scriesWhenEnteringCreatureGainsMatchingTypeFromXenograft() {
        addCreatureReady(player1, new ValleyQuestcaller());
        Permanent xenograft = addCreatureReady(player1, new Xenograft());
        xenograft.setChosenSubtype(CardSubtype.RABBIT);
        harness.setLibrary(player1, List.of(new ShrikeForce()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, findPermanent(player1, "Grizzly Bears"), CardSubtype.RABBIT))
                .isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scriesForBatEntry() {
        addCreatureReady(player1, new ValleyQuestcaller());
        assertEntryScriesOnce(new StarscapeCleric(), 1, ManaColor.BLACK);
    }
}
