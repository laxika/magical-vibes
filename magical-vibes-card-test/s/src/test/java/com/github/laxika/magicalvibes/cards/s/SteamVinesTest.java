package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Annex;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
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

@CardUsed({SteamVines.class, DuskImp.class, Mountain.class, Annex.class})
class SteamVinesTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast Steam Vines targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DuskImp());
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
        resolveAllTriggers();

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
        resolveAllTriggers();

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
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tappedLand, aura);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mountain"))
                .anyMatch(card -> card.getName().equals("Steam Vines"));
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Mountain());
    }

    private Permanent attachAura(Player auraController, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new SteamVines());
        aura.setAttachedTo(host.getId());
        return aura;
    }

    @Test
    @DisplayName("The tap trigger still destroys the land and deals damage if Steam Vines leaves first")
    void triggerResolvesAfterAuraLeavesBattlefield() {
        Permanent tappedLand = addLand(player1);
        Permanent aura = attachAura(player1, tappedLand);
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tappedLand);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Steam Vines"));
    }

    @Test
    @DisplayName("Casting Steam Vines attaches it to the targeted land")
    void resolvesAttachedToTargetLand() {
        Permanent land = addLand(player2);
        harness.setHand(player1, List.of(new SteamVines()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Steam Vines").getAttachedTo()).isEqualTo(land.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Steam Vines triggers again when its new land becomes tapped")
    void triggersAfterMovingToAnotherLand() {
        Permanent firstLand = addLand(player1);
        Permanent secondLand = addLand(player2);
        Permanent aura = attachAura(player1, firstLand);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player1, 0);
        resolveAllTriggers();
        assertThat(aura.getAttachedTo()).isEqualTo(secondLand.getId());

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstLand, aura);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(secondLand);
        harness.assertInGraveyard(player1, "Steam Vines");
    }

    @Test
    @DisplayName("Regeneration does not prevent damage or require choosing a different land")
    void regeneratedLandCanKeepAura() {
        Permanent land = addLand(player1);
        Permanent aura = attachAura(player1, land);
        land.setRegenerationShield(1);
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land, aura);
        assertThat(land.getRegenerationShield()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(aura.getAttachedTo()).isEqualTo(land.getId());
    }

    @Test
    @DisplayName("Damage uses the land's controller immediately before destruction")
    void damagesControllerAtResolutionAfterControlChanges() {
        addLand(player1);
        Permanent land = tapLandThenEndAnnexControl();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The land's controller at resolution chooses the next land")
    void controllerAtResolutionChoosesAttachmentAfterControlChanges() {
        Permanent nextLand = addLand(player1);
        addLand(player2);
        tapLandThenEndAnnexControl();

        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handlePermanentChosen(player2, nextLand.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Steam Vines").getAttachedTo()).isEqualTo(nextLand.getId());
    }

    private Permanent tapLandThenEndAnnexControl() {
        Permanent land = addLand(player2);
        Permanent annex = harness.addToBattlefieldAndReturn(player1, new Annex());
        annex.setAttachedTo(land.getId());
        attachAura(player1, land);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(land));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, annex));
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        return land;
    }
}
