package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.t.Threaten;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BomburGentleDreamer.class, FountainOfYouth.class, Threaten.class, Lignify.class})
class BomburGentleDreamerTest extends BaseCardTest {

    @Test
    void doesNotUntapBeforeEnduringStory() {
        Permanent bombur = harness.enterBattlefieldAndReturn(player1, new BomburGentleDreamer());
        bombur.tap();

        harness.performUntapStep(player1);

        assertThat(bombur.isTapped()).isTrue();
    }

    @Test
    void untapsAfterEnduringStory() {
        Permanent bombur = harness.enterBattlefieldAndReturn(player1, new BomburGentleDreamer());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        bombur.tap();

        assertThat(gd.playersWithEnduringStory).contains(player1.getId());
        harness.performUntapStep(player1);

        assertThat(bombur.isTapped()).isFalse();
    }

    @Test
    void twoQualifyingPermanentsAreNotEnough() {
        Permanent bombur = harness.enterBattlefieldAndReturn(player1, new BomburGentleDreamer());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        bombur.tap();

        harness.performUntapStep(player1);

        assertThat(gd.playersWithEnduringStory).doesNotContain(player1.getId());
        assertThat(bombur.isTapped()).isTrue();
    }

    @Test
    void opponentsArtifactsDoNotCount() {
        Permanent bombur = harness.enterBattlefieldAndReturn(player1, new BomburGentleDreamer());
        harness.enterBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player2, new FountainOfYouth());
        bombur.tap();

        harness.performUntapStep(player1);

        assertThat(gd.playersWithEnduringStory).doesNotContain(player1.getId());
        assertThat(bombur.isTapped()).isTrue();
    }

    @Test
    void enteringBomburCountsItselfTowardExistingArtifacts() {
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        assertThat(gd.playersWithEnduringStory).doesNotContain(player1.getId());

        Permanent bombur = harness.enterBattlefieldAndReturn(player1, new BomburGentleDreamer());
        bombur.tap();
        harness.performUntapStep(player1);

        assertThat(gd.playersWithEnduringStory).contains(player1.getId());
        assertThat(bombur.isTapped()).isFalse();
    }

    @Test
    void enduringStoryPersistsAfterArtifactsLeave() {
        Permanent bombur = harness.enterBattlefieldAndReturn(player1, new BomburGentleDreamer());
        Permanent firstArtifact = harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent secondArtifact = harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, firstArtifact);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, secondArtifact);
        });
        bombur.tap();

        harness.performUntapStep(player1);

        assertThat(gd.playersWithEnduringStory).contains(player1.getId());
        assertThat(bombur.isTapped()).isFalse();
    }

    @Test
    void gainingControlOfBomburGrantsEnduringStoryImmediately() {
        Permanent bombur = harness.enterBattlefieldAndReturn(player2, new BomburGentleDreamer());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Threaten()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, bombur.getId());

        assertThat(gd.findControllerOf(bombur)).isEqualTo(player1.getId());
        assertThat(gd.playersWithEnduringStory).contains(player1.getId());
        assertThat(gd.playersWithEnduringStory).doesNotContain(player2.getId());
        bombur.tap();
        harness.performUntapStep(player1);
        assertThat(bombur.isTapped()).isFalse();
    }

    @Test
    void losingAbilitiesRemovesUntapRestriction() {
        Permanent bombur = harness.enterBattlefieldAndReturn(player1, new BomburGentleDreamer());
        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, bombur.getId());
        harness.passBothPriorities();
        bombur.tap();

        harness.performUntapStep(player1);

        assertThat(gd.playersWithEnduringStory).doesNotContain(player1.getId());
        assertThat(bombur.isTapped()).isFalse();
    }

    @Test
    void removedStoriedAbilityDoesNotGrantEnduringStory() {
        Permanent bombur = harness.enterBattlefieldAndReturn(player1, new BomburGentleDreamer());
        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, bombur.getId());
        harness.passBothPriorities();

        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(gd.playersWithEnduringStory).doesNotContain(player1.getId());
    }
}
