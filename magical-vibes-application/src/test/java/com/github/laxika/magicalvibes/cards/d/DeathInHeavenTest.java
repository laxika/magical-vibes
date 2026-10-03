package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AdiposeOffspring;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TrueNameNemesis;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathInHeaven.class, Forest.class, GrizzlyBears.class, AdiposeOffspring.class,
        TrueNameNemesis.class})
class DeathInHeavenTest extends BaseCardTest {

    @Test
    void millsAndExilesTheTargetPlayersGraveyardOnChaptersOneAndTwoThenReturnsTrackedCreatures() {
        Card graveyardCreature = new GrizzlyBears();
        Card graveyardLand = new Forest();
        Card milledCreature = new GrizzlyBears();
        Card milledLand = new Forest();
        harness.setGraveyard(player2, List.of(graveyardCreature, graveyardLand));
        harness.setLibrary(player2, List.of(milledCreature, milledLand));

        Permanent saga = addSagaWithLore(0);

        advanceToNextChapter();
        chooseTargetPlayer();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(saga.getId()))
                .containsExactlyInAnyOrder(graveyardCreature, graveyardLand, milledCreature, milledLand);

        advanceToNextChapter();
        chooseTargetPlayer();

        advanceToNextChapter();
        harness.passBothPriorities();

        List<Permanent> cybermen = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .toList();
        assertThat(cybermen).hasSize(2);
        assertThat(cybermen).allSatisfy(permanent -> {
            assertThat(permanent.getFaceDownPower()).isEqualTo(2);
            assertThat(permanent.getFaceDownToughness()).isEqualTo(2);
            assertThat(gqs.getEffectiveCardTypes(gd, permanent))
                    .containsExactlyInAnyOrder(CardType.ARTIFACT, CardType.CREATURE);
            assertThat(gqs.hasEffectiveSubtype(gd, permanent, CardSubtype.CYBERMAN)).isTrue();
        });
        assertThat(cybermen)
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(graveyardCreature.getId(), milledCreature.getId());
        assertThat(gd.getCardsExiledByPermanent(saga.getId()))
                .containsExactlyInAnyOrder(graveyardLand, milledLand);
        harness.assertNotOnBattlefield(player1, "Death in Heaven");
    }

    @Test
    void canTargetItsControllerAndExilesOnlyThatPlayersGraveyard() {
        Card creature = new AdiposeOffspring();
        Card opponentLand = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(opponentLand));
        Permanent saga = addSagaWithLore(0);

        advanceToNextChapter();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(saga.getId())).hasSize(2).contains(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentLand);
    }

    @Test
    void faceDownReturnDoesNotTriggerPrintedEntersAbilities() {
        Card creature = new AdiposeOffspring();
        harness.setGraveyard(player2, List.of(creature));
        harness.setLibrary(player2, List.of());
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        chooseTargetPlayer();
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard().getId()).isEqualTo(creature.getId());
            assertThat(permanent.isFaceDown()).isTrue();
        });
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void secondChapterTracksNewCardsButDoesNotReturnUnrelatedExiledCreatures() {
        Card firstCreature = new AdiposeOffspring();
        Card secondCreature = new AdiposeOffspring();
        Card unrelatedCreature = new AdiposeOffspring();
        harness.setExile(player2, List.of(unrelatedCreature));
        harness.setGraveyard(player2, List.of(firstCreature));
        harness.setLibrary(player2, List.of());
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        chooseTargetPlayer();

        harness.setLibrary(player2, List.of(secondCreature));
        advanceToNextChapter();
        chooseTargetPlayer();
        assertThat(gd.getCardsExiledByPermanent(saga.getId()))
                .containsExactlyInAnyOrder(firstCreature, secondCreature);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(firstCreature.getId(), secondCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void faceDownReturnDoesNotRequirePrintedAsEntersPlayerChoice() {
        Card creature = new TrueNameNemesis();
        harness.setGraveyard(player2, List.of(creature));
        harness.setLibrary(player2, List.of());
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        chooseTargetPlayer();
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard().getId()).isEqualTo(creature.getId());
            assertThat(permanent.isFaceDown()).isTrue();
        });
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new DeathInHeaven());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void chooseTargetPlayer() {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)
                .validPlayerIds()).contains(player2.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));
        harness.passBothPriorities();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
