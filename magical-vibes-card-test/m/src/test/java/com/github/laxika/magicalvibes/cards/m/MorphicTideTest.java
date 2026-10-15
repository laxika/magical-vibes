package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrbOfDreams;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MorphicTide.class, Panopticon.class, GrizzlyBears.class, GloriousAnthem.class,
        Pacifism.class, OrbOfDreams.class})
class MorphicTideTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlanechase() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.deck.add(new Panopticon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void shufflesOwnedPermanentsAndReturnsCreatureAndEnchantmentPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        gd.planechase.deck.addFirst(new MorphicTide());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Glorious Anthem");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void putsUnmatchedRevealedCardsOnTheBottomInChosenOrder() {
        Card firstBattle = battle("First battle");
        Card secondBattle = battle("Second battle");
        harness.addToBattlefieldAndReturn(player1, firstBattle)
                .setCounterCount(CounterType.DEFENSE, 5);
        harness.addToBattlefieldAndReturn(player1, secondBattle)
                .setCounterCount(CounterType.DEFENSE, 5);
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        gd.planechase.deck.addFirst(new MorphicTide());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactlyInAnyOrder(firstBattle, secondBattle);

        int firstIndex = reorder.cards().indexOf(firstBattle);
        int secondIndex = reorder.cards().indexOf(secondBattle);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(secondIndex, firstIndex)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondBattle, firstBattle);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void revealedAuraAttachesToCreatureFromFirstBatch() {
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        gd.planechase.deck.addFirst(new MorphicTide());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        var returnedCreature = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Grizzly Bears"));
        var returnedAura = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Pacifism"));
        assertThat(returnedCreature).isNotNull();
        assertThat(returnedAura).isNotNull();
        assertThat(returnedAura.getAttachedTo()).isEqualTo(returnedCreature.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void auraWithoutLegalAttachmentIsPutOnBottomInsteadOfEntering() {
        Pacifism pacifism = new Pacifism();
        Card token = new GrizzlyBears();
        token.setToken(true);
        harness.addToBattlefield(player1, token);
        harness.setLibrary(player1, List.of(pacifism));
        harness.setLibrary(player2, List.of());
        gd.planechase.deck.addFirst(new MorphicTide());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(pacifism);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(pacifism);
    }

    @Test
    void firstBatchReplacementEffectsApplyToLaterEnchantmentBatch() {
        harness.addToBattlefield(player1, new OrbOfDreams());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        gd.planechase.deck.addFirst(new MorphicTide());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, harness.getPermanentId(player1, "Orb of Dreams")).isTapped()).isFalse();
        assertThat(gqs.findPermanentById(gd, harness.getPermanentId(player1, "Glorious Anthem")).isTapped()).isTrue();
    }

    @Test
    void opposingAuraIsShuffledEvenWhenItsHostOwnerIsProcessedFirst() {
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Pacifism pacifism = new Pacifism();
        harness.addToBattlefieldAndReturn(player2, pacifism).setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        gd.planechase.deck.addFirst(new MorphicTide());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(pacifism);
        var returnedAura = gqs.findPermanentById(gd, harness.getPermanentId(player2, "Pacifism"));
        assertThat(returnedAura).isNotNull();
        assertThat(returnedAura.getAttachedTo())
                .isEqualTo(gqs.findPermanentById(gd, harness.getPermanentId(player1, "Grizzly Bears")).getId());
    }

    private Card battle(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.BATTLE);
        card.setDefense(5);
        return card;
    }
}
