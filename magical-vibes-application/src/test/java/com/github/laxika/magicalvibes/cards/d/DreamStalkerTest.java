package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamStalker.class, BenalishCavalry.class, PrismaticLens.class})
class DreamStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts a non-targeting choice among all permanents you control")
    void etbPromptsBounceAmongOwnPermanents() {
        UUID lensId = harness.addToBattlefieldAndReturn(player1, new PrismaticLens()).getId();
        UUID cavalryId = harness.addToBattlefieldAndReturn(player1, new BenalishCavalry()).getId();
        castAndResolveSpell();

        UUID dreamStalkerId = harness.getPermanentId(player1, "Dream Stalker");
        resolveTriggerToChoice();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(lensId, cavalryId, dreamStalkerId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("Choosing a permanent returns it to its owner's hand")
    void bounceOtherPermanent() {
        UUID lensId = harness.addToBattlefieldAndReturn(player1, new PrismaticLens()).getId();
        castAndResolveSpell();
        resolveTriggerToChoice();

        harness.handlePermanentChosen(player1, lensId);

        harness.assertNotOnBattlefield(player1, "Prismatic Lens");
        harness.assertInHand(player1, "Prismatic Lens");
        harness.assertOnBattlefield(player1, "Dream Stalker");
    }

    @Test
    @DisplayName("It can return itself when it is the only permanent you control")
    void bounceSelf() {
        castAndResolveSpell();
        UUID dreamStalkerId = harness.getPermanentId(player1, "Dream Stalker");
        resolveTriggerToChoice();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(dreamStalkerId);

        harness.handlePermanentChosen(player1, dreamStalkerId);

        harness.assertNotOnBattlefield(player1, "Dream Stalker");
        harness.assertInHand(player1, "Dream Stalker");
    }

    @Test
    @DisplayName("Opponent permanents are not valid choices")
    void opponentPermanentsExcluded() {
        harness.addToBattlefield(player2, new BenalishCavalry());
        castAndResolveSpell();
        UUID dreamStalkerId = harness.getPermanentId(player1, "Dream Stalker");
        resolveTriggerToChoice();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(dreamStalkerId);
        harness.assertOnBattlefield(player2, "Benalish Cavalry");
    }

    private void castAndResolveSpell() {
        harness.castFromHand(player1, new DreamStalker(), "{1}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A controlled permanent owned by the opponent returns to the opponent's hand")
    void returnsBorrowedPermanentToOwner() {
        var lens = harness.addToBattlefieldAndReturn(player2, new PrismaticLens());
        UUID lensId = lens.getId();
        harness.inMutationScope(() -> com.github.laxika.magicalvibes.testutil.GameTestEngineContext.get()
                .getBean(com.github.laxika.magicalvibes.service.battlefield.CreatureControlService.class)
                .applyControlEffect(gd, player1.getId(), lens,
                        new com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect(
                                com.github.laxika.magicalvibes.model.effect.ControlDuration.PERMANENT),
                        com.github.laxika.magicalvibes.model.effect.EffectDuration.PERMANENT, null, "borrowed permanent"));
        castAndResolveSpell();
        resolveTriggerToChoice();

        harness.handlePermanentChosen(player1, lensId);

        harness.assertNotOnBattlefield(player1, "Prismatic Lens");
        harness.assertInHand(player2, "Prismatic Lens");
        harness.assertNotInHand(player1, "Prismatic Lens");
        harness.assertOnBattlefield(player1, "Dream Stalker");
    }

    @Test
    @DisplayName("The trigger still returns another permanent after Dream Stalker leaves")
    void triggerResolvesAfterSourceLeaves() {
        UUID lensId = harness.addToBattlefieldAndReturn(player1, new PrismaticLens()).getId();
        castAndResolveSpell();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, findPermanent(player1, "Dream Stalker")));

        resolveTriggerToChoice();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(lensId);
        harness.handlePermanentChosen(player1, lensId);
        harness.assertInHand(player1, "Prismatic Lens");
        harness.assertNotOnBattlefield(player1, "Prismatic Lens");
        harness.assertInGraveyard(player1, "Dream Stalker");
    }

    @Test
    @DisplayName("The trigger does nothing if no controlled permanents remain")
    void noPermanentsAtResolution() {
        harness.addToBattlefield(player2, new PrismaticLens());
        castAndResolveSpell();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, findPermanent(player1, "Dream Stalker")));

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Prismatic Lens");
        harness.assertNotInHand(player2, "Prismatic Lens");
        harness.assertInGraveyard(player1, "Dream Stalker");
    }

    private void resolveTriggerToChoice() {
        harness.passBothPriorities();
    }
}
