package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConjurersMantle.class, GrizzlyBears.class, LlanowarElves.class, Plains.class, Shock.class})
class ConjurersMantleTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 and vigilance")
    void equippedCreatureGetsBoostAndVigilance() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mantle = addMantle(player1);
        mantle.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Attack trigger offers a matching creature card and puts the choice into hand")
    void attackTriggerOffersMatchingCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mantle = addMantle(player1);
        mantle.setAttachedTo(creature.getId());
        Card matching = new GrizzlyBears();
        Card nonmatching = new LlanowarElves();
        Card instant = new Shock();
        Card land = new Plains();
        Card otherNonmatching = new LlanowarElves();
        Card otherInstant = new Shock();
        harness.setLibrary(player1, List.of(matching, nonmatching, instant, land,
                otherNonmatching, otherInstant));

        declareMantleAttackers(player1, List.of(0));
        resolveAttackTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(matching.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(matching);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                nonmatching, instant, land, otherNonmatching, otherInstant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Only the top six cards are considered")
    void onlyLooksAtTopSixCards() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mantle = addMantle(player1);
        mantle.setAttachedTo(creature.getId());
        Card matchingBelowLook = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new LlanowarElves(), new Shock(), new Plains(),
                new LlanowarElves(), new Shock(), new Plains(), matchingBelowLook));

        declareMantleAttackers(player1, List.of(0));
        resolveAttackTrigger();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(matchingBelowLook);
    }

    @Test
    @DisplayName("Does not trigger when unattached")
    void doesNotTriggerWhenUnattached() {
        addCreatureReady(player1, new GrizzlyBears());
        addMantle(player1);

        declareMantleAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard() instanceof ConjurersMantle);
    }

    private Permanent addMantle(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ConjurersMantle());
    }

    private void declareMantleAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, Map.of());
    }

    private void resolveAttackTrigger() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
