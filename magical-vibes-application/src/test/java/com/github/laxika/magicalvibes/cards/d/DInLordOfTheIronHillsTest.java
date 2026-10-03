package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StealArtifact;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DInLordOfTheIronHills.class, FountainOfYouth.class, GrizzlyBears.class, StealArtifact.class})
class DInLordOfTheIronHillsTest extends BaseCardTest {

    @Test
    void doesNotTaxAttacksBeforeEnduringStory() {
        addDainWithArtifacts(1);
        addReadyAttackers(1);

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void taxesEachCreatureAfterControllingThreeQualifyingPermanents() {
        addDainWithArtifacts(2);
        addReadyAttackers(2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void cannotAttackWithoutPayingTheTax() {
        addDainWithArtifacts(2);
        addReadyAttackers(1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    void enduringStoryRemainsAfterQualifyingPermanentsLeave() {
        addDainWithArtifacts(2);
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Fountain of Youth"));
        addReadyAttackers(1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    private void addDainWithArtifacts(int artifactCount) {
        harness.enterBattlefieldAndReturn(player1, new DInLordOfTheIronHills());
        for (int i = 0; i < artifactCount; i++) {
            harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        }
    }

    private void addReadyAttackers(int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player2, new GrizzlyBears());
        }
    }

    @Test
    void gainsEnduringStoryWhenThirdQualifyingPermanentChangesController() {
        addDainWithArtifacts(1);
        Permanent artifact = harness.enterBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new StealArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        addReadyAttackers(1);
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    void gainsEnduringStoryWhenEnteringWithTwoArtifactsAlreadyPresent() {
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new DInLordOfTheIronHills());
        addReadyAttackers(1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    void opponentsArtifactsDoNotContributeToEnduringStory() {
        addDainWithArtifacts(1);
        addReadyAttackers(1);
        harness.enterBattlefieldAndReturn(player2, new FountainOfYouth());

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void enduringStoryDoesNotKeepAttackTaxAfterDainLeaves() {
        addDainWithArtifacts(2);
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof DInLordOfTheIronHills);
        addReadyAttackers(1);

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
