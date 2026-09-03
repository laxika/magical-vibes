package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({Kudzu.class, Mountain.class, GrizzlyBears.class})
class KudzuTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast Kudzu targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
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
        resolveUntilMayChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, destination.getId());
        resolveStackFully();

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
        resolveUntilMayChoice();

        harness.handleMayAbilityChosen(player2, false);
        resolveStackFully();

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
        resolveUntilMayChoice();

        harness.handleMayAbilityChosen(player2, true);
        resolveStackFully();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(tappedLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Kudzu"));
    }

    private Permanent addLand(Player player) {
        harness.addToBattlefield(player, new Mountain());
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        return battlefield.get(battlefield.size() - 1);
    }

    private Permanent attachAura(Player auraController, Permanent host) {
        Permanent aura = new Permanent(new Kudzu());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
        return aura;
    }

    private void resolveUntilMayChoice() {
        for (int i = 0; i < 10; i++) {
            if (gd.interaction.activeInteraction() != null) {
                return;
            }
            if (gd.stack.isEmpty() && gd.pendingManaAbilityTriggers.isEmpty()
                    && gd.pendingMayAbilities.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    private void resolveStackFully() {
        for (int i = 0; i < 10 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
