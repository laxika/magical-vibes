package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChainLightning.class, BarbaryApes.class})
class ChainLightningTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a targeted player when they decline the copy payment")
    void dealsDamageToPlayerWhenPaymentDeclined() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A targeted player who pays {R}{R} controls the copy")
    void targetedPlayerControlsPaidCopy() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("The targeted permanent's controller is offered the copy payment")
    void targetedPermanentControllerIsOfferedPayment() {
        Permanent target = addCreatureReady(player2, new BarbaryApes());
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The copy may be retargeted to another player")
    void copiedSpellMayBeRetargetedToAnotherPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId());

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting the copy payment without {R}{R} creates no copy")
    void acceptingWithoutManaCreatesNoCopy() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An illegal target makes Chain Lightning do nothing and prevents copying")
    void illegalTargetPreventsDamageAndCopy() {
        Permanent target = addCreatureReady(player2, new BarbaryApes());
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Paying the mana still allows the player to decline creating a copy")
    void mayDeclineCopyAfterPaying() {
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A lethally damaged creature's controller can copy and retarget before it dies")
    void lethallyDamagedCreatureControllerCanCopy() {
        Permanent target = addCreatureReady(player2, new BarbaryApes());
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.assertOnBattlefield(player2, "Barbary Apes");
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());

        harness.assertInGraveyard(player2, "Barbary Apes");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A copy can itself be copied and sent back to the other player")
    void copyCanContinueTheChain() {
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 14);
        assertThat(gd.stack).isEmpty();
    }
}
