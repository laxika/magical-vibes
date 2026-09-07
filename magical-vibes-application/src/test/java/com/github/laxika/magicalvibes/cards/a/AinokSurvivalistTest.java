package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AinokSurvivalist.class, FountainOfYouth.class, GloriousAnthem.class, GrizzlyBears.class})
class AinokSurvivalistTest extends BaseCardTest {

    @Test
    void turningFaceUpCanDestroyAnOpponentArtifactOrEnchantment() {
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent survivalist = castFaceDown();

        turnFaceUp(survivalist);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(opponentArtifact.getId(), opponentEnchantment.getId());
        harness.handlePermanentChosen(player1, opponentArtifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
    }

    @Test
    void turningFaceUpHasNoTargetWhenOnlyOwnArtifactsOrEnchantmentsExist() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent survivalist = castFaceDown();

        turnFaceUp(survivalist);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(survivalist.isFaceDown()).isFalse();
        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new AinokSurvivalist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Ainok Survivalist");
    }

    private void turnFaceUp(Permanent survivalist) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(survivalist));
    }
}
