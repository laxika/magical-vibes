package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChooseYourChampion.class, GrizzlyBears.class, Shock.class})
class ChooseYourChampionTest extends BaseCardTest {

    @Test
    @DisplayName("The targeted opponent chooses the player who shares the restriction")
    void targetedOpponentChoosesAllowedPlayer() {
        resolveScheme();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player2, player1.getId());

        assertThat(harness.getCastingPermissionService()
                .isPlayerPreventedFromCasting(gd, player1.getId())).isFalse();
        assertThat(harness.getCastingPermissionService()
                .isPlayerPreventedFromCasting(gd, player2.getId())).isTrue();
        assertThat(harness.getAttackLegalityService()
                .isPlayerPreventedFromAttacking(gd, player1.getId())).isFalse();
        assertThat(harness.getAttackLegalityService()
                .isPlayerPreventedFromAttacking(gd, player2.getId())).isTrue();
    }

    @Test
    @DisplayName("The restriction ends at the controller's next turn")
    void restrictionEndsAtControllersNextTurn() {
        resolveScheme();
        harness.handlePermanentChosen(player2, player1.getId());
        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThat(harness.getCastingPermissionService()
                .isPlayerPreventedFromCasting(gd, player2.getId())).isFalse();
        assertThat(harness.getAttackLegalityService()
                .isPlayerPreventedFromAttacking(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("A player allowed by the choice can still cast and attack")
    void chosenPlayerCanCastAndAttack() {
        resolveScheme();
        harness.handlePermanentChosen(player2, player2.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        assertThat(harness.getAttackLegalityService()
                .canAttack(gd, attacker, player2.getId())).isTrue();
    }

    @Test
    void disallowedPlayerCannotCastOrAttack() {
        resolveScheme();
        harness.handlePermanentChosen(player2, player1.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        addCreatureReady(player2, new GrizzlyBears());
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void restrictionPersistsThroughOpponentsTurnAndExpiresOnControllersTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        resolveScheme();
        harness.handlePermanentChosen(player2, player1.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(harness.getCastingPermissionService()
                .isPlayerPreventedFromCasting(gd, player2.getId())).isTrue();
        assertThat(harness.getAttackLegalityService()
                .isPlayerPreventedFromAttacking(gd, player2.getId())).isTrue();

        harness.passUntil(player1, TurnStep.UPKEEP);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        assertThat(harness.getAttackLegalityService()
                .isPlayerPreventedFromAttacking(gd, player2.getId())).isFalse();
    }

    @Test
    void controllerCannotMakeTheTargetedOpponentsChoice() {
        resolveScheme();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");

        harness.handlePermanentChosen(player2, player2.getId());

        assertThat(harness.getCastingPermissionService()
                .isPlayerPreventedFromCasting(gd, player1.getId())).isFalse();
        assertThat(harness.getCastingPermissionService()
                .isPlayerPreventedFromCasting(gd, player2.getId())).isFalse();
        assertThat(harness.getAttackLegalityService()
                .isPlayerPreventedFromAttacking(gd, player1.getId())).isFalse();
        assertThat(harness.getAttackLegalityService()
                .isPlayerPreventedFromAttacking(gd, player2.getId())).isFalse();
    }

    private void resolveScheme() {
        ChooseYourChampion scheme = new ChooseYourChampion();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL),
                player2.getId(),
                (Zone) null));
        harness.passBothPriorities();
    }
}
