package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.z.ZulaportCutthroat;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicAberration.class, GoblinPiker.class, GrizzlyBears.class, Ornithopter.class,
        ZulaportCutthroat.class})
class AngelicAberrationTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices selected creatures with base power or toughness 1 or less")
    void sacrificesSmallCreaturesAndCreatesMatchingAngels() {
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());
        ornithopter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent goblinPiker = addCreatureReady(player1, new GoblinPiker());
        goblinPiker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent grizzlyBears = addCreatureReady(player1, new GrizzlyBears());
        grizzlyBears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.castFromHand(player1, new AngelicAberration(), "{5}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.SacrificeAnyNumberAndRecordCount.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(ornithopter.getId(), goblinPiker.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(ornithopter.getId(), goblinPiker.getId()));

        assertThat(findPermanents(player1, "Eldrazi Angel")).hasSize(2).allSatisfy(angel -> {
            assertThat(angel.getCard().getColors()).isEmpty();
            assertThat(angel.getCard().getSubtypes()).containsExactlyInAnyOrder(
                    CardSubtype.ELDRAZI,
                    CardSubtype.ANGEL);
            assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
            assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
        });
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(grizzlyBears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Ornithopter", "Goblin Piker");
    }

    @Test
    @DisplayName("Can choose zero even when eligible creatures are available")
    void canSacrificeNoCreatures() {
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());

        harness.castFromHand(player1, new AngelicAberration(), "{5}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ornithopter);
        assertThat(findPermanents(player1, "Eldrazi Angel")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Can sacrifice a subset and cannot choose an opponent's creatures")
    void sacrificesOnlyTheChosenSubset() {
        Permanent chosen = addCreatureReady(player1, new Ornithopter());
        Permanent retained = addCreatureReady(player1, new GoblinPiker());
        Permanent opposing = addCreatureReady(player2, new Ornithopter());

        harness.castFromHand(player1, new AngelicAberration(), "{5}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(chosen.getId(), retained.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(retained).doesNotContain(chosen);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposing);
        assertThat(findPermanents(player1, "Eldrazi Angel")).hasSize(1);
        assertThat(findPermanents(player2, "Eldrazi Angel")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Ornithopter");
    }

    @Test
    @DisplayName("Resolves without creating tokens when only the opponent has eligible creatures")
    void noEligibleCreaturesCreatesNoTokens() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new Ornithopter());

        harness.castFromHand(player1, new AngelicAberration(), "{5}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposing);
        assertThat(findPermanents(player1, "Eldrazi Angel")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrificed death watchers see every creature sacrificed simultaneously")
    void sacrificedCutthroatSeesAllSimultaneousDeaths() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent cutthroat = addCreatureReady(player1, new ZulaportCutthroat());
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());

        harness.castFromHand(player1, new AngelicAberration(), "{5}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(cutthroat.getId(), ornithopter.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cutthroat, ornithopter);
        assertThat(findPermanents(player1, "Eldrazi Angel")).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
