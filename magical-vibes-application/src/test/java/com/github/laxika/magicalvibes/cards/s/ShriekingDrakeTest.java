package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.cards.f.FuneralCharm;
import com.github.laxika.magicalvibes.cards.t.TeferisPuzzleBox;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShriekingDrake.class, CloudElemental.class, TeferisPuzzleBox.class, FuneralCharm.class})
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

    @Test
    @DisplayName("Drake can return itself even when another creature is available")
    void bounceSelfWithAnotherCreatureAvailable() {
        harness.addToBattlefield(player1, new CloudElemental());
        castAndResolveSpell();
        UUID drakeId = harness.getPermanentId(player1, "Shrieking Drake");
        resolveTriggerToChoice();

        harness.handlePermanentChosen(player1, drakeId);

        harness.assertInHand(player1, "Shrieking Drake");
        harness.assertNotOnBattlefield(player1, "Shrieking Drake");
        harness.assertOnBattlefield(player1, "Cloud Elemental");
    }

    @Test
    @DisplayName("A creature controlled by you but owned by the opponent returns to their hand")
    void bounceStolenCreatureToOwnersHand() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new CloudElemental());
        gd.stolenCreatures.put(elemental.getId(), player2.getId());
        castAndResolveSpell();
        resolveTriggerToChoice();

        harness.handlePermanentChosen(player1, elemental.getId());

        harness.assertNotOnBattlefield(player1, "Cloud Elemental");
        harness.assertInHand(player2, "Cloud Elemental");
        harness.assertNotInHand(player1, "Cloud Elemental");
        harness.assertOnBattlefield(player1, "Shrieking Drake");
    }

    @Test
    @DisplayName("The trigger still returns another creature after Drake dies in response")
    void triggerResolvesAfterDrakeDies() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new CloudElemental());
        castAndResolveSpell();
        killDrakeInResponse();

        resolveTriggerToChoice();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(elemental.getId());
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.assertInHand(player1, "Cloud Elemental");
        harness.assertInGraveyard(player1, "Shrieking Drake");
    }

    @Test
    @DisplayName("The trigger resolves without a choice when Drake dies and no creatures remain")
    void noCreaturesAtResolution() {
        harness.addToBattlefield(player2, new CloudElemental());
        castAndResolveSpell();
        killDrakeInResponse();

        resolveTriggerToChoice();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Shrieking Drake");
        harness.assertOnBattlefield(player2, "Cloud Elemental");
        harness.assertNotInHand(player2, "Cloud Elemental");
    }

    private void killDrakeInResponse() {
        UUID drakeId = harness.getPermanentId(player1, "Shrieking Drake");
        harness.setHand(player1, List.of(new FuneralCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, 1, drakeId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Shrieking Drake");
    }

    private void castAndResolveSpell() {
        harness.castFromHand(player1, new ShriekingDrake(), "{U}");
        harness.passBothPriorities();
    }

    private void resolveTriggerToChoice() {
        harness.passBothPriorities();
    }
}
