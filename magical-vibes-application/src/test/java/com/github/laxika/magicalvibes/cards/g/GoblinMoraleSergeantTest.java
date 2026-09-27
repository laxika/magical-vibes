package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinMoraleSergeant.class, GrizzlyBears.class, Forest.class})
class GoblinMoraleSergeantTest extends BaseCardTest {

    @Test
    void enlistingNontokenCreatureOffersDuplicateIntoTopFive() {
        addCreatureReady(player1, new GoblinMoraleSergeant());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());
        List<Card> library = List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        List<Card> updatedLibrary = gd.playerDecks.get(player1.getId());
        assertThat(updatedLibrary).hasSize(6);
        int duplicateIndex = java.util.stream.IntStream.range(0, updatedLibrary.size())
                .filter(index -> updatedLibrary.get(index).getName().equals("Grizzly Bears")
                        && updatedLibrary.get(index).isTokenCard())
                .findFirst().orElseThrow();
        assertThat(duplicateIndex).isLessThan(5);
    }

    @Test
    void duplicatePerpetuallyGetsPowerAndHaste() {
        addCreatureReady(player1, new GoblinMoraleSergeant());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getPriorityPlayerId(gd)).isEqualTo(player1.getId());
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent duplicate = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isTokenCard())
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, duplicate)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, duplicate, Keyword.HASTE)).isTrue();
    }

    @Test
    void tokenEnlistedCreatureDoesNotTrigger() {
        addCreatureReady(player1, new GoblinMoraleSergeant());
        Card tokenBear = new GrizzlyBears();
        tokenBear.setToken(true);
        Permanent supporter = addCreatureReady(player1, tokenBear);

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
