package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurtleDuck;
import com.github.laxika.magicalvibes.cards.v.VedalkenOrrery;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoggySwampVisions.class, GrizzlyBears.class, TurtleDuck.class, VedalkenOrrery.class})
class FoggySwampVisionsTest extends BaseCardTest {

    @Test
    void copiesCreatureCardsFromAllGraveyardsAndSacrificesTokensAtNextEndStep() {
        Permanent waterbendSourceOne = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent waterbendSourceTwo = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setHand(player1, List.of(new FoggySwampVisions()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        gs.playCard(gd, player1, 0, 2, null, null,
                List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null,
                List.of(waterbendSourceOne.getId(), waterbendSourceTwo.getId()));

        assertThat(waterbendSourceOne.isTapped()).isTrue();
        assertThat(waterbendSourceTwo.isTapped()).isTrue();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownCreature.getId(), opposingCreature.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opposingCreature);

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void xZeroDoesNotCreateTokens() {
        harness.setHand(player1, List.of(new FoggySwampVisions()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void paysWaterbendEntirelyWithManaAndCopiesOnlyTheChosenCreature() {
        Card chosen = new GrizzlyBears();
        Card unchosen = new GrizzlyBears();
        Card noncreature = new FoggySwampVisions();
        harness.setGraveyard(player2, List.of(chosen, unchosen, noncreature));
        harness.setHand(player1, List.of(new FoggySwampVisions()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(chosen.getId(), unchosen.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().isToken()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(unchosen, noncreature);
        assertThat(gd.playerExiledCards.get(player2.getId())).contains(chosen);
    }

    @Test
    void createsOnlyOneCopyWhenOneOfTwoTargetsLeavesTheGraveyard() {
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setHand(player1, List.of(new FoggySwampVisions()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId(), opposingCreature.getId()));

        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(opposingCreature));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerExiledCards.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opposingCreature);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotResolveWhenAllTargetsLeaveTheGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new FoggySwampVisions()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
        harness.assertInGraveyard(player1, "Foggy Swamp Visions");
    }

    @Test
    void tokensSurviveOpponentsEndStepUntilControllersNextEndStep() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new FoggySwampVisions()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        harness.assertOnBattlefield(player1, "Vedalken Orrery");
    }

    @Test
    void allowsXAboveOneHundredWhenTheCostAndTargetsAreAvailable() {
        List<Card> creatures = IntStream.range(0, 101)
                .mapToObj(i -> (Card) new GrizzlyBears()).toList();
        harness.setGraveyard(player2, creatures);
        harness.setHand(player1, List.of(new FoggySwampVisions()));
        harness.addMana(player1, ManaColor.BLACK, 104);

        harness.castSorcery(player1, 0, 101);
        harness.handleMultipleCardsChosen(player1, creatures.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(101);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void canTapAnArtifactAndPayTheRestOfWaterbendWithMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new VedalkenOrrery());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first, second));
        harness.setHand(player1, List.of(new FoggySwampVisions()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        gs.playCard(gd, player1, 0, 2, null, null,
                List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null,
                List.of(artifact.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(artifact);
    }

    @Test
    void tokenCopiesRetainTheOriginalCreaturesActivatedAbilities() {
        Card creature = new TurtleDuck();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new FoggySwampVisions()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }
}
