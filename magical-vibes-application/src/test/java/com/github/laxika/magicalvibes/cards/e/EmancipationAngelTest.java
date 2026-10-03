package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ScrollOfAvacyn;
import com.github.laxika.magicalvibes.cards.w.WanderingWolf;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmancipationAngel.class, ScrollOfAvacyn.class, WanderingWolf.class, Plains.class})
class EmancipationAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Entering prompts for any permanent you control, including itself")
    void promptIncludesAllOwnPermanents() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ScrollOfAvacyn());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new WanderingWolf());

        harness.castFromHand(player1, new EmancipationAngel(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        UUID angelId = harness.getPermanentId(player1, "Emancipation Angel");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(angelId, artifact.getId(), creature.getId())
                .doesNotContain(opponentPermanent.getId());
    }

    @Test
    @DisplayName("Chosen permanent is returned to its owner's hand")
    void chosenPermanentReturnedToHand() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ScrollOfAvacyn());

        harness.castFromHand(player1, new EmancipationAngel(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof ScrollOfAvacyn);
    }

    @Test
    @DisplayName("It can return itself when it is the only permanent you control")
    void canReturnItself() {
        harness.castFromHand(player1, new EmancipationAngel(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        UUID angelId = harness.getPermanentId(player1, "Emancipation Angel");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(angelId);

        harness.handlePermanentChosen(player1, angelId);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof EmancipationAngel);
    }

    @Test
    @DisplayName("A land can be chosen and returned")
    void canReturnLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.castFromHand(player1, new EmancipationAngel(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(land.getId());
        harness.handlePermanentChosen(player1, land.getId());

        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertInHand(player1, "Plains");
        harness.assertOnBattlefield(player1, "Emancipation Angel");
    }

    @Test
    @DisplayName("A controlled permanent owned by an opponent goes to that opponent's hand")
    void returnsBorrowedPermanentToOwner() {
        Permanent borrowed = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        gd.stolenCreatures.put(borrowed.getId(), player2.getId());
        harness.castFromHand(player1, new EmancipationAngel(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, borrowed.getId());

        harness.assertNotOnBattlefield(player1, "Wandering Wolf");
        harness.assertNotInHand(player1, "Wandering Wolf");
        harness.assertInHand(player2, "Wandering Wolf");
    }

    @Test
    @DisplayName("The trigger still returns another permanent after the Angel leaves")
    void triggerResolvesAfterAngelLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.castFromHand(player1, new EmancipationAngel(), "{1}{W}{W}");
        harness.passBothPriorities();
        Permanent angel = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof EmancipationAngel)
                .findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, angel));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.assertInHand(player1, "Plains");
        harness.assertInHand(player1, "Emancipation Angel");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger finishes without a choice if no controlled permanents remain")
    void noChoiceWhenAngelLeavesEmptyBattlefield() {
        harness.castFromHand(player1, new EmancipationAngel(), "{1}{W}{W}");
        harness.passBothPriorities();
        Permanent angel = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, angel));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Emancipation Angel");
    }
}
