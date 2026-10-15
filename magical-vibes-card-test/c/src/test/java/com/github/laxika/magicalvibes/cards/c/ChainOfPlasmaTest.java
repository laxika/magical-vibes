package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChainOfPlasma.class, ElvishWarrior.class})
class ChainOfPlasmaTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage, then the target player may discard and copy it")
    void targetPlayerMayDiscardAndCopy() {
        harness.setHand(player2, List.of(
                new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior()));
        castAt(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(mayChoicePlayer()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        assertThat(discardChoicePlayer()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(mayChoicePlayer()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("A permanent's controller may discard and copy it")
    void targetPermanentControllerMayDiscardAndCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.setHand(player2, List.of(new ElvishWarrior(), new ElvishWarrior()));
        castAt(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(mayChoicePlayer()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Declining to discard does not create a copy")
    void decliningDiscardDoesNotCopy() {
        harness.setHand(player2, List.of(new ElvishWarrior()));
        castAt(player2.getId());

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the copy after discarding does not create another spell")
    void decliningCopyAfterDiscardDoesNotCreateAnotherSpell() {
        harness.setHand(player2, List.of(new ElvishWarrior()));
        castAt(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A copy may choose a new target and retains Chain of Plasma's discard ability")
    void copyMayChooseNewTargetAndRetainsDiscardAbility() {
        harness.setHand(player2, List.of(new ElvishWarrior()));
        castAt(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.setHand(player1, List.of(new ElvishWarrior()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(mayChoicePlayer()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Accepting with no cards to discard creates no copy")
    void acceptingWithNoCardsToDiscardCreatesNoCopy() {
        harness.setHand(player2, List.of());
        castAt(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining new targets preserves the original permanent target")
    void copyKeepsPermanentTargetWhenRetargetingIsDeclined() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.setHand(player2, List.of(new ElvishWarrior()));
        castAt(target.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(target.getId());
        harness.assertNotOnBattlefield(player2, "Elvish Warrior");
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining to discard for a copy finishes resolution without another copy choice")
    void copyCannotBeCopiedWithoutAnotherDiscard() {
        harness.setHand(player2, List.of(new ElvishWarrior(), new ElvishWarrior()));
        castAt(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        assertThat(mayChoicePlayer()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An illegal original target prevents both damage and the discard choice")
    void illegalTargetPreventsDiscardChoice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.setHand(player2, List.of(new ElvishWarrior()));
        harness.setHand(player1, List.of(new ChainOfPlasma()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    private void castAt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ChainOfPlasma()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private java.util.UUID mayChoicePlayer() {
        return gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId();
    }

    private java.util.UUID discardChoicePlayer() {
        return gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId();
    }
}
