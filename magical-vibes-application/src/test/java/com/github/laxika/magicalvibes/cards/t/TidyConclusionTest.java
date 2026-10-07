package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrakhataPillarBug;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TidyConclusion.class, FountainOfYouth.class, GrizzlyBears.class, PrakhataPillarBug.class})
class TidyConclusionTest extends BaseCardTest {

    @Test
    void destroysTargetCreatureAndGainsLifeForEachArtifactControlled() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TidyConclusion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 15);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 17);
    }

    @Test
    void cannotTargetNoncreatureArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TidyConclusion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Fountain of Youth")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsOnlyRemainingArtifactsAfterDestroyingOwnArtifactCreature() {
        harness.addToBattlefield(player1, new PrakhataPillarBug());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TidyConclusion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 15);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Prakhata Pillar-Bug"));

        harness.assertInGraveyard(player1, "Prakhata Pillar-Bug");
        harness.assertNotOnBattlefield(player1, "Prakhata Pillar-Bug");
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    void gainsNoLifeWithoutArtifactsEvenWhenOpponentControlsArtifacts() {
        harness.addToBattlefield(player2, new PrakhataPillarBug());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TidyConclusion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 15);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Prakhata Pillar-Bug"));

        harness.assertInGraveyard(player2, "Prakhata Pillar-Bug");
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
    }

    @Test
    void gainsNoLifeWhenOnlyTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TidyConclusion(), new TidyConclusion()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setLife(player1, 15);
        var targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 16);

        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertInGraveyard(player1, "Tidy Conclusion");
    }
}
