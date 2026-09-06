package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gemrazer.class, AngelicChorus.class, FountainOfYouth.class})
class GemrazerTest extends BaseCardTest {

    @Test
    void mutatingDestroysTargetArtifactAnOpponentControls() {
        Permanent gemrazer = addCreatureReady(player1, new Gemrazer());
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        triggerMutation(gemrazer);
        harness.handlePermanentChosen(player1, fountain.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    void mutatingDestroysTargetEnchantmentAnOpponentControls() {
        Permanent gemrazer = addCreatureReady(player1, new Gemrazer());
        Permanent chorus = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());

        triggerMutation(gemrazer);
        harness.handlePermanentChosen(player1, chorus.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    void mutatingCannotTargetOwnArtifact() {
        Permanent gemrazer = addCreatureReady(player1, new Gemrazer());
        Permanent ownFountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new FountainOfYouth());

        triggerMutation(gemrazer);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownFountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    private void triggerMutation(Permanent gemrazer) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, gemrazer, List.of(gemrazer.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
