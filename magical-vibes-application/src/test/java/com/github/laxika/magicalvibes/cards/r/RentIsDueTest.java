package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RentIsDue.class, GrizzlyBears.class})
class RentIsDueTest extends BaseCardTest {

    @Test
    void tapsCreatureAndTreasureToDraw() {
        harness.addToBattlefield(player1, new RentIsDue());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent treasure = addTreasureToken(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        resolveEndStepTrigger(true);

        assertThat(creature.isTapped()).isTrue();
        assertThat(treasure.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Rent Is Due");
    }

    @Test
    void sacrificesWhenTwoEligiblePermanentsCannotBeTapped() {
        harness.addToBattlefield(player1, new RentIsDue());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent artifact = addArtifact(player1);

        resolveEndStepTrigger(true);

        harness.assertInGraveyard(player1, "Rent Is Due");
        assertThat(creature.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    void decliningToTapSacrificesRentIsDue() {
        harness.addToBattlefield(player1, new RentIsDue());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addTreasureToken(player1);

        resolveEndStepTrigger(false);

        harness.assertInGraveyard(player1, "Rent Is Due");
        assertThat(creature.isTapped()).isFalse();
    }

    private void resolveEndStepTrigger(boolean accept) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }

    private Permanent addTreasureToken(Player player) {
        Card treasureCard = new Card();
        treasureCard.setName("Treasure");
        treasureCard.setType(CardType.ARTIFACT);
        treasureCard.setSubtypes(List.of(CardSubtype.TREASURE));
        treasureCard.setToken(true);

        Permanent treasure = new Permanent(treasureCard);
        treasure.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(treasure);
        return treasure;
    }

    private Permanent addArtifact(Player player) {
        Card artifactCard = new Card();
        artifactCard.setName("Artifact");
        artifactCard.setType(CardType.ARTIFACT);

        Permanent artifact = new Permanent(artifactCard);
        artifact.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(artifact);
        return artifact;
    }
}
