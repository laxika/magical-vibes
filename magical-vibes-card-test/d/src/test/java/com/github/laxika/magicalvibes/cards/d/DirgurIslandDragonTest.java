package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AnkhOfMishra;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DirgurIslandDragon.class, AnkhOfMishra.class, GrizzlyBears.class})
class DirgurIslandDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Omen taps up to one creature, draws a card, and shuffles the card into its owner's library")
    void omenTapsCreatureDrawsAndShuffles() {
        DirgurIslandDragon card = new DirgurIslandDragon();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castWithAlternateCost(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Omen may be cast without choosing a creature")
    void omenMayDeclineCreatureTarget() {
        DirgurIslandDragon card = new DirgurIslandDragon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Omen only allows a creature as its optional target")
    void omenRejectsNonCreatureTarget() {
        DirgurIslandDragon card = new DirgurIslandDragon();
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new AnkhOfMishra());
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() ->
                harness.castWithAlternateCost(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void omenDrawsBeforeReturningExactlyOnePhysicalCardToLibrary() {
        DirgurIslandDragon card = new DirgurIslandDragon();
        GrizzlyBears draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    void illegalSoleTargetPreventsDrawAndShuffle() {
        DirgurIslandDragon card = new DirgurIslandDragon();
        GrizzlyBears draw = new GrizzlyBears();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castWithAlternateCost(player1, 0, creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    void omenRejectsMoreThanOneCreatureTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DirgurIslandDragon()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(gd, player1, 0, 0,
                null, null, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void omenRejectsNonCreatureInTargetList() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AnkhOfMishra());
        harness.setHand(player1, List.of(new DirgurIslandDragon()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(gd, player1, 0, 0,
                null, null, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceResolvesOntoBattlefield() {
        DirgurIslandDragon card = new DirgurIslandDragon();
        harness.castFromHand(player1, card, "{5}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dirgur Island Dragon");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    void decliningWardCountersOpponentsOmenWithoutDrawingOrShuffling() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new DirgurIslandDragon());
        DirgurIslandDragon card = new DirgurIslandDragon();
        GrizzlyBears draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, dragon.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(dragon.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    void payingWardAllowsOpponentsOmenToResolve() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new DirgurIslandDragon());
        harness.setHand(player1, List.of(new DirgurIslandDragon()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, dragon.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(dragon.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void ownDragonDoesNotTriggerWardAndAlreadyTappedTargetStillAllowsDraw() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DirgurIslandDragon());
        dragon.tap();
        harness.setHand(player1, List.of(new DirgurIslandDragon()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castWithAlternateCost(player1, 0, dragon.getId());
        harness.passBothPriorities();

        assertThat(dragon.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
