package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CliffThreader;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
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

@CardUsed({KorSkyfisher.class, Island.class, CliffThreader.class, IntoTheRoil.class})
class KorSkyfisherTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts a non-targeting choice among all permanents you control")
    void etbPromptsBounceAmongOwnPermanents() {
        UUID islandId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        UUID threaderId = harness.addToBattlefieldAndReturn(player1, new CliffThreader()).getId();
        castAndResolveSpell();

        UUID skyfisherId = harness.getPermanentId(player1, "Kor Skyfisher");
        resolveTriggerToChoice();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(islandId, threaderId, skyfisherId);
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
        harness.assertOnBattlefield(player1, "Kor Skyfisher");
    }

    @Test
    @DisplayName("It can return itself when it is the only permanent you control")
    void bounceSelf() {
        castAndResolveSpell();
        UUID skyfisherId = harness.getPermanentId(player1, "Kor Skyfisher");
        resolveTriggerToChoice();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(skyfisherId);

        harness.handlePermanentChosen(player1, skyfisherId);

        harness.assertNotOnBattlefield(player1, "Kor Skyfisher");
        harness.assertInHand(player1, "Kor Skyfisher");
    }

    @Test
    @DisplayName("Opponent permanents are not valid choices")
    void opponentPermanentsExcluded() {
        harness.addToBattlefield(player2, new CliffThreader());
        castAndResolveSpell();
        UUID skyfisherId = harness.getPermanentId(player1, "Kor Skyfisher");
        resolveTriggerToChoice();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(skyfisherId);
        harness.assertOnBattlefield(player2, "Cliff Threader");
    }

    @Test
    @DisplayName("A controlled permanent owned by an opponent returns to that opponent's hand")
    void returnsBorrowedPermanentToOwner() {
        CliffThreader borrowed = new CliffThreader();
        borrowed.setOwnerId(player2.getId());
        UUID borrowedId = harness.addToBattlefieldAndReturn(player1, borrowed).getId();
        castAndResolveSpell();
        resolveTriggerToChoice();

        harness.handlePermanentChosen(player1, borrowedId);

        harness.assertNotOnBattlefield(player1, "Cliff Threader");
        harness.assertInHand(player2, "Cliff Threader");
        harness.assertNotInHand(player1, "Cliff Threader");
        harness.assertOnBattlefield(player1, "Kor Skyfisher");
    }

    @Test
    @DisplayName("The trigger still returns another permanent after Skyfisher leaves")
    void triggerResolvesAfterSourceLeaves() {
        UUID islandId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        castAndResolveSpell();
        returnSkyfisherInResponse();
        resolveTriggerToChoice();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(islandId);
        harness.handlePermanentChosen(player1, islandId);

        harness.assertInHand(player1, "Island");
        harness.assertInHand(player1, "Kor Skyfisher");
        harness.assertNotOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("The trigger resolves without a choice when no permanents remain")
    void triggerResolvesWithNoPermanents() {
        castAndResolveSpell();
        returnSkyfisherInResponse();
        resolveTriggerToChoice();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInHand(player1, "Kor Skyfisher");
    }

    private void returnSkyfisherInResponse() {
        UUID skyfisherId = harness.getPermanentId(player1, "Kor Skyfisher");
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, skyfisherId);
    }

    private void castAndResolveSpell() {
        harness.castFromHand(player1, new KorSkyfisher(), "{1}{W}");

        harness.passBothPriorities();
    }

    private void resolveTriggerToChoice() {
        harness.passBothPriorities();
    }
}
