package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SteamVinesTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast Steam Vines targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new SteamVines()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Destroying the tapped land deals damage and moves the Aura to the only other land")
    void destroysDamagesAndMovesAura() {
        Permanent tappedLand = addLand(player1);
        Permanent otherLand = addLand(player1);
        Permanent aura = attachAura(player1, tappedLand);
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        resolveStackFully();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tappedLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getName().equals("Mountain"));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(aura.getAttachedTo()).isEqualTo(otherLand.getId());
    }

    @Test
    @DisplayName("The tapped land's controller can move the Aura to a land controlled by another player")
    void choosesAnyLand() {
        Permanent tappedLand = addLand(player2);
        Permanent otherLand = addLand(player2);
        Permanent landControlledByAuraController = addLand(player1);
        Permanent aura = attachAura(player1, tappedLand);
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveUntilChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, landControlledByAuraController.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(tappedLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura, landControlledByAuraController);
        assertThat(aura.getAttachedTo()).isEqualTo(landControlledByAuraController.getId());
    }

    @Test
    @DisplayName("With no other land, the destroyed land's Aura goes to its owner's graveyard")
    void noLandToMoveTo() {
        Permanent tappedLand = addLand(player1);
        Permanent aura = attachAura(player1, tappedLand);
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        resolveStackFully();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tappedLand, aura);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mountain"))
                .anyMatch(card -> card.getName().equals("Steam Vines"));
    }

    private Permanent addLand(Player player) {
        harness.addToBattlefield(player, new Mountain());
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        return battlefield.get(battlefield.size() - 1);
    }

    private Permanent attachAura(Player auraController, Permanent host) {
        Permanent aura = new Permanent(new SteamVines());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
        return aura;
    }

    private void resolveUntilChoice() {
        for (int i = 0; i < 8; i++) {
            if (gd.interaction.activeInteraction() != null) {
                return;
            }
            if (gd.stack.isEmpty() && gd.pendingManaAbilityTriggers.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
