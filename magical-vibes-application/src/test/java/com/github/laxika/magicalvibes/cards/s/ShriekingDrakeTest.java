package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.cards.t.TeferisPuzzleBox;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShriekingDrake.class, CloudElemental.class, TeferisPuzzleBox.class})
class ShriekingDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts a non-targeting choice among creatures you control, including itself")
    void etbPromptsBounceAmongOwnCreaturesIncludingSelf() {
        harness.addToBattlefield(player1, new CloudElemental());
        UUID elementalId = harness.getPermanentId(player1, "Cloud Elemental");
        castAndResolveSpell();

        UUID drakeId = harness.getPermanentId(player1, "Shrieking Drake");
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
        resolveTriggerToChoice();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(elementalId, drakeId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("Choosing another creature returns it to hand; Drake stays")
    void bounceOtherCreature() {
        harness.addToBattlefield(player1, new CloudElemental());
        UUID elementalId = harness.getPermanentId(player1, "Cloud Elemental");
        castAndResolveSpell();
        resolveTriggerToChoice();

        harness.handlePermanentChosen(player1, elementalId);

        harness.assertNotOnBattlefield(player1, "Cloud Elemental");
        harness.assertInHand(player1, "Cloud Elemental");
        harness.assertOnBattlefield(player1, "Shrieking Drake");
    }

    @Test
    @DisplayName("May bounce itself; alone, itself is the only choice")
    void bounceSelf() {
        castAndResolveSpell();
        UUID drakeId = harness.getPermanentId(player1, "Shrieking Drake");
        resolveTriggerToChoice();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(drakeId);

        harness.handlePermanentChosen(player1, drakeId);

        harness.assertNotOnBattlefield(player1, "Shrieking Drake");
        harness.assertInHand(player1, "Shrieking Drake");
    }

    @Test
    @DisplayName("Opponent creatures and non-creatures are not valid choices")
    void opponentAndNoncreaturesExcluded() {
        harness.addToBattlefield(player1, new TeferisPuzzleBox());
        harness.addToBattlefield(player2, new CloudElemental());
        castAndResolveSpell();
        UUID drakeId = harness.getPermanentId(player1, "Shrieking Drake");
        resolveTriggerToChoice();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(drakeId);
        harness.assertOnBattlefield(player2, "Cloud Elemental");
    }

    private void castAndResolveSpell() {
        harness.setHand(player1, List.of(new ShriekingDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void resolveTriggerToChoice() {
        harness.passBothPriorities();
    }
}
