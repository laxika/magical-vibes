package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({DreamStalker.class, GrizzlyBears.class, Island.class})
class DreamStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts a non-targeting choice among all permanents you control")
    void etbPromptsBounceAmongOwnPermanents() {
        UUID islandId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        castAndResolveSpell();

        UUID dreamStalkerId = harness.getPermanentId(player1, "Dream Stalker");
        resolveTriggerToChoice();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(islandId, bearsId, dreamStalkerId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("Choosing a permanent returns it to its owner's hand")
    void bounceOtherPermanent() {
        UUID islandId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        castAndResolveSpell();
        resolveTriggerToChoice();

        harness.handlePermanentChosen(player1, islandId);

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Island");
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
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAndResolveSpell();
        UUID dreamStalkerId = harness.getPermanentId(player1, "Dream Stalker");
        resolveTriggerToChoice();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(dreamStalkerId);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castAndResolveSpell() {
        harness.setHand(player1, List.of(new DreamStalker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void resolveTriggerToChoice() {
        harness.passBothPriorities();
    }
}
