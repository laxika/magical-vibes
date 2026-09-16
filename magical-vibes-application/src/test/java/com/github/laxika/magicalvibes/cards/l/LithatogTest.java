package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CatalystStone;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Lithatog.class, Forest.class, CatalystStone.class})
class LithatogTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact gives Lithatog +1/+1 until end of turn")
    void sacrificeArtifactBoostsLithatog() {
        Permanent lithatog = harness.addToBattlefieldAndReturn(player1, new Lithatog());
        harness.addToBattlefield(player1, new CatalystStone());
        harness.addToBattlefield(player1, new Forest());
        int powerBefore = gqs.getEffectivePower(gd, lithatog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, lithatog);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Catalyst Stone");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gqs.getEffectivePower(gd, lithatog)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, lithatog)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    @DisplayName("Sacrificing a land gives Lithatog +1/+1 until end of turn")
    void sacrificeLandBoostsLithatog() {
        Permanent lithatog = harness.addToBattlefieldAndReturn(player1, new Lithatog());
        harness.addToBattlefield(player1, new CatalystStone());
        harness.addToBattlefield(player1, new Forest());
        int powerBefore = gqs.getEffectivePower(gd, lithatog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, lithatog);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Catalyst Stone");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gqs.getEffectivePower(gd, lithatog)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, lithatog)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent lithatog = harness.addToBattlefieldAndReturn(player1, new Lithatog());
        harness.addToBattlefield(player1, new CatalystStone());
        int powerBefore = gqs.getEffectivePower(gd, lithatog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, lithatog);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, lithatog)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, lithatog)).isEqualTo(toughnessBefore);
    }

    @Test
    @DisplayName("An ability cannot be activated without a matching permanent to sacrifice")
    void cannotActivateWithoutMatchingPermanent() {
        harness.addToBattlefield(player1, new Lithatog());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: a land");
    }

    @Test
    @DisplayName("Each ability lets you choose among matching permanents")
    void choosesMatchingPermanents() {
        Permanent lithatog = harness.addToBattlefieldAndReturn(player1, new Lithatog());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new CatalystStone());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new CatalystStone());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        int powerBefore = gqs.getEffectivePower(gd, lithatog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, lithatog);

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.PermanentChoice artifactChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(artifactChoice).isNotNull();
        assertThat(artifactChoice.validIds())
                .containsExactlyInAnyOrder(firstArtifact.getId(), secondArtifact.getId());

        harness.handlePermanentChosen(player1, secondArtifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(firstArtifact)
                .doesNotContain(secondArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondArtifact.getCard());

        harness.activateAbility(player1, 0, 1, null, null);

        PendingInteraction.PermanentChoice landChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(landChoice).isNotNull();
        assertThat(landChoice.validIds())
                .containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());

        harness.handlePermanentChosen(player1, firstLand.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(secondLand)
                .doesNotContain(firstLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstLand.getCard());
        assertThat(gqs.getEffectivePower(gd, lithatog)).isEqualTo(powerBefore + 2);
        assertThat(gqs.getEffectiveToughness(gd, lithatog)).isEqualTo(toughnessBefore + 2);
    }

    @Test
    @DisplayName("Only permanents controlled by Lithatog's controller can be sacrificed")
    void cannotSacrificeOpponentsPermanents() {
        harness.addToBattlefield(player1, new Lithatog());
        harness.addToBattlefield(player2, new CatalystStone());
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: a land");
    }
}
