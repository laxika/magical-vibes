package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VariableSolutions.class, Forest.class, MindStone.class})
class VariableSolutionsTest extends BaseCardTest {

    @Test
    void xOneSeeksBasicLandTapped() {
        harness.setHand(player1, List.of(new VariableSolutions()));
        harness.setLibrary(player1, List.of(new Forest()));
        cast(1);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    void xTwoMakesEachOpponentSacrificeAnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setHand(player1, List.of(new VariableSolutions()));
        cast(2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player2, List.of(artifact.getId()));
        harness.assertInGraveyard(player2, "Mind Stone");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherArtifact);
    }

    @Test
    void xThreeConjuresAPermanentCreatureWithManaValueThree() {
        harness.setHand(player1, List.of(new VariableSolutions()));
        cast(3);

        List<Permanent> conjured = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.CREATURE))
                .toList();
        assertThat(conjured).hasSize(1);
        Permanent creature = conjured.getFirst();
        assertThat(creature.getCard().getManaValue()).isEqualTo(3);
        assertThat(gd.getDelayedActions(com.github.laxika.magicalvibes.model.action.DelayedPermanentAction.class))
                .isEmpty();
    }

    @Test
    void xFourOrMoreDoesAllModesAndGainsLife() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new VariableSolutions()));
        int startingLife = gd.getLife(player1.getId());
        cast(4);

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife + 2);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player2, List.of(artifact.getId()));
        harness.assertInGraveyard(player2, "Mind Stone");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherArtifact);

        List<Permanent> creatures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.CREATURE))
                .toList();
        assertThat(creatures).hasSize(1);
        assertThat(creatures.getFirst().getCard().getManaValue()).isEqualTo(4);
    }

    @Test
    void xZeroDoesNothingEvenWhenOtherBranchesHaveResources() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new VariableSolutions()));
        int startingLife = gd.getLife(player1.getId());

        cast(0);

        harness.assertLife(player1, startingLife);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Variable Solutions");
    }

    @Test
    void xOneWithoutABasicLandDoesNotSeekANonland() {
        MindStone nonland = new MindStone();
        harness.setLibrary(player1, List.of(nonland));
        harness.setHand(player1, List.of(new VariableSolutions()));

        cast(1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Variable Solutions");
    }

    @Test
    void xOneOnlySeeksOneLandAndDoesNotPerformOtherBranches() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new VariableSolutions()));
        int startingLife = gd.getLife(player1.getId());

        cast(1);

        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(artifact);
        harness.assertLife(player1, startingLife);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void xTwoDoesNotSacrificeControllersArtifactsOrOpponentsLands() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Forest libraryLand = new Forest();
        harness.setLibrary(player1, List.of(libraryLand));
        harness.setHand(player1, List.of(new VariableSolutions()));
        int startingLife = gd.getLife(player1.getId());

        cast(2);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposingLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryLand);
        harness.assertLife(player1, startingLife);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Variable Solutions");
    }

    @Test
    void xFiveStillGainsTwoLifeAndConjuresWhenEarlierEffectsCannotAct() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new VariableSolutions()));
        int startingLife = gd.getLife(player1.getId());

        cast(5);

        harness.assertLife(player1, startingLife + 2);
        List<Permanent> creatures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.CREATURE))
                .toList();
        assertThat(creatures).hasSize(1);
        assertThat(creatures.getFirst().getCard().getManaValue()).isEqualTo(5);
        assertThat(creatures.getFirst().getCard().isToken()).isFalse();
        assertThat(creatures.getFirst().getCard().getOwnerId()).isEqualTo(player1.getId());
    }

    private void cast(int xValue) {
        harness.addMana(player1, ManaColor.GREEN, xValue + 1);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }
}
