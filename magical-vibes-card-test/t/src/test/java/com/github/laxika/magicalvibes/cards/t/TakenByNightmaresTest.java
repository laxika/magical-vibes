package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.Mintstrosity;
import com.github.laxika.magicalvibes.cards.h.HopefulVigil;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TakenByNightmares.class, Mintstrosity.class, HopefulVigil.class})
class TakenByNightmaresTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target creature without scrying when you control no enchantment")
    void exilesCreatureWithoutEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        castTakenByNightmares(player1, target.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInGraveyard(player1, "Taken by Nightmares");
    }

    @Test
    @DisplayName("Exiles a target creature and scries 2 when you control an enchantment")
    void exilesCreatureAndScriesWithEnchantment() {
        harness.addToBattlefield(player1, new HopefulVigil());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        harness.setLibrary(player1, List.of(new Mintstrosity(), new Mintstrosity()));
        castTakenByNightmares(player1, target.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Taken by Nightmares");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HopefulVigil());
        harness.setHand(player1, List.of(new TakenByNightmares()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsEnchantmentDoesNotEnableScry() {
        harness.addToBattlefield(player2, new HopefulVigil());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        harness.setLibrary(player1, List.of(new Mintstrosity(), new TakenByNightmares()));
        castTakenByNightmares(player1, target.getId());

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Taken by Nightmares");
    }

    @Test
    void enchantmentLeavingBeforeResolutionPreventsScry() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new HopefulVigil());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        harness.setLibrary(player1, List.of(new Mintstrosity(), new TakenByNightmares()));
        castTakenByNightmares(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, enchantment));

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Taken by Nightmares");
    }

    @Test
    void enchantmentEnteringBeforeResolutionEnablesScryAndBottomChoice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        TakenByNightmares top = new TakenByNightmares();
        Mintstrosity second = new Mintstrosity();
        harness.setLibrary(player1, List.of(top, second));
        castTakenByNightmares(player1, target.getId());
        harness.addToBattlefield(player1, new HopefulVigil());

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(top, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Taken by Nightmares");
    }

    @Test
    void missingTargetPreventsScryEvenWithEnchantment() {
        harness.addToBattlefield(player1, new HopefulVigil());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        harness.setLibrary(player1, List.of(new Mintstrosity(), new TakenByNightmares()));
        castTakenByNightmares(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, target));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Taken by Nightmares");
    }

    @Test
    void canExileOwnCreatureWithoutTriggeringItsDeathAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Mintstrosity());
        castTakenByNightmares(player1, target.getId());

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Mintstrosity");
        harness.assertInGraveyard(player1, "Taken by Nightmares");
    }

    @Test
    void scryWithOneCardInLibraryUsesOnlyThatCard() {
        harness.addToBattlefield(player1, new HopefulVigil());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        Mintstrosity onlyCard = new Mintstrosity();
        harness.setLibrary(player1, List.of(onlyCard));
        castTakenByNightmares(player1, target.getId());

        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Taken by Nightmares");
    }

    @Test
    void emptyLibraryDoesNotPreventExileOrFinishOfResolution() {
        harness.addToBattlefield(player1, new HopefulVigil());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        harness.setLibrary(player1, List.of());
        castTakenByNightmares(player1, target.getId());

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Taken by Nightmares");
    }

    private void castTakenByNightmares(Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new TakenByNightmares()));
        addMana(caster);
        harness.castInstant(caster, 0, targetId);
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
