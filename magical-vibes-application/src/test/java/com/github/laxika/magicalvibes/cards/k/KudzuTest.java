package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.ShiftingBorders;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Kudzu.class, Mountain.class, GrizzlyBears.class, ShiftingBorders.class})
class KudzuTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast Kudzu targeting a non-land permanent")
    void cannotTargetNonLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Kudzu()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("The tapped land's controller may move Kudzu to a land controlled by another player")
    void tappedLandControllerMovesAura() {
        Permanent tappedLand = addLand(player2);
        Permanent otherLand = addLand(player2);
        Permanent destination = addLand(player1);
        Permanent aura = attachAura(player1, tappedLand);

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, destination.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(tappedLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura, destination);
        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
    }

    @Test
    @DisplayName("Declining to move Kudzu leaves it unattached and sends it to its owner's graveyard")
    void declinesToMoveAura() {
        Permanent tappedLand = addLand(player2);
        addLand(player2);
        Permanent aura = attachAura(player1, tappedLand);

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(tappedLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Kudzu"));
    }

    @Test
    @DisplayName("Accepting to move Kudzu with no land available leaves it in its owner's graveyard")
    void acceptsMoveWithNoLandAvailable() {
        Permanent tappedLand = addLand(player2);
        Permanent aura = attachAura(player1, tappedLand);

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(tappedLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Kudzu"));
    }

    @Test
    @DisplayName("Casting Kudzu attaches it to a land and its trigger can move it to the only remaining land")
    void castsAndMovesToOnlyRemainingLand() {
        Permanent host = addLand(player2);
        Permanent destination = addLand(player1);
        harness.setHand(player1, List.of(new Kudzu()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Kudzu");
        assertThat(aura.getAttachedTo()).isEqualTo(host.getId());

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(host);
    }

    @Test
    @CardUsed({Kudzu.class, Mountain.class, ShiftingBorders.class})
    @DisplayName("The land's controller immediately before destruction chooses whether and where Kudzu moves")
    void changedLandControllerChoosesMove() {
        Permanent host = addLand(player2);
        Permanent exchangedLand = addLand(player1);
        Permanent destination = addLand(player2);
        Permanent aura = attachAura(player1, host);
        harness.setHand(player1, List.of(new ShiftingBorders()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.tapPermanent(player2, 0);
        harness.castAndResolveInstant(player1, 0, List.of(host.getId(), exchangedLand.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(host);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player1.getId());
        harness.handlePermanentChosen(player1, destination.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(host);
        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Mountain());
    }

    private Permanent attachAura(Player auraController, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new Kudzu());
        aura.setAttachedTo(host.getId());
        return aura;
    }
}
