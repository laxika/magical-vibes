package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TophTheFirstMetalbender.class, Forest.class, GrizzlyBears.class, SolRing.class})
class TophTheFirstMetalbenderTest extends BaseCardTest {

    @Test
    void turnsYourNontokenArtifactsIntoLands() {
        harness.addToBattlefield(player1, new TophTheFirstMetalbender());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent ownNonArtifact = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent artifactToken = harness.addToBattlefieldAndReturn(player1, artifactToken());

        assertThat(gqs.isLand(gd, ownArtifact)).isTrue();
        assertThat(gqs.isLand(gd, ownNonArtifact)).isFalse();
        assertThat(gqs.isLand(gd, opponentArtifact)).isFalse();
        assertThat(gqs.isLand(gd, artifactToken)).isFalse();
        assertThat(gqs.isArtifact(ownArtifact)).isTrue();
    }

    @Test
    void earthbendsAChosenLandAtYourEndStep() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new TophTheFirstMetalbender());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());

        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private static Card artifactToken() {
        Card card = new Card();
        card.setName("Artifact Token");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("");
        card.setToken(true);
        return card;
    }
}
