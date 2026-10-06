package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KarametrasFavor;
import com.github.laxika.magicalvibes.cards.n.NyxbornWolf;
import com.github.laxika.magicalvibes.cards.u.UnravelTheAether;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SetessanStarbreaker.class, SwordwiseCentaur.class, KarametrasFavor.class,
        NyxbornWolf.class, UnravelTheAether.class})
class SetessanStarbreakerTest extends BaseCardTest {

    private Permanent addAura(Player controller) {
        Permanent host = harness.addToBattlefieldAndReturn(controller, new SwordwiseCentaur());
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new KarametrasFavor());
        aura.setAttachedTo(host.getId());
        return aura;
    }

    private void castAndAcceptMay(UUID auraId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SetessanStarbreaker()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, auraId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("ETB prompts to destroy an Aura when one exists")
    void etbPromptsToDestroyAura() {
        addAura(player2);
        harness.setHand(player1, List.of(new SetessanStarbreaker()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Karametra's Favor"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the ETB destroys the target Aura")
    void acceptingEtbDestroysAura() {
        UUID auraId = addAura(player2).getId();
        castAndAcceptMay(auraId);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Karametra's Favor");
        harness.assertInGraveyard(player2, "Karametra's Favor");
        harness.assertOnBattlefield(player2, "Swordwise Centaur");
    }

    @Test
    @DisplayName("Declining the ETB leaves the Aura on the battlefield")
    void decliningEtbLeavesAura() {
        addAura(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SetessanStarbreaker()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Karametra's Favor"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Karametra's Favor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB does not prompt when no Aura exists")
    void etbDoesNotPromptWithoutAura() {
        harness.addToBattlefield(player2, new SwordwiseCentaur());
        harness.setHand(player1, List.of(new SetessanStarbreaker()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Setessan Starbreaker");
        harness.assertOnBattlefield(player2, "Swordwise Centaur");
    }

    @Test
    @DisplayName("ETB can destroy an Aura controlled by its controller")
    void etbCanDestroyOwnAura() {
        UUID auraId = addAura(player1).getId();
        castAndAcceptMay(auraId);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Karametra's Favor");
        harness.assertInGraveyard(player1, "Karametra's Favor");
    }

    @Test
    @DisplayName("An enchantment creature that is not bestowed is not an Aura target")
    void enchantmentCreatureIsNotAnAuraTarget() {
        harness.addToBattlefield(player2, new NyxbornWolf());
        harness.setHand(player1, List.of(new SetessanStarbreaker()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Nyxborn Wolf");
        harness.assertOnBattlefield(player1, "Setessan Starbreaker");
    }

    @Test
    @DisplayName("Only the selected Aura is destroyed when multiple Auras exist")
    void destroysOnlySelectedAura() {
        Permanent selected = addAura(player2);
        Permanent other = addAura(player2);

        castAndAcceptMay(selected.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(other.getId()).doesNotContain(selected.getId());
        harness.assertInGraveyard(player2, "Karametra's Favor");
    }

    @Test
    @DisplayName("A target that leaves before resolution prevents the optional destruction choice")
    void missingTargetDoesNotPromptOrDestroyAnotherAura() {
        Permanent selected = addAura(player2);
        Permanent other = addAura(player2);
        harness.setHand(player1, List.of(new SetessanStarbreaker(), new UnravelTheAether()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, selected.getId());
        harness.castAndResolveInstant(player1, 0, selected.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(other.getId()).doesNotContain(selected.getId());
        assertThat(gd.playerDecks.get(player2.getId())).extracting(card -> card.getName())
                .contains("Karametra's Favor");
    }

    @Test
    @DisplayName("A bestowed creature is an Aura and can be destroyed")
    void destroysBestowedAura() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        harness.setHand(player1, List.of(new NyxbornWolf()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        castAndAcceptMay(harness.getPermanentId(player1, "Nyxborn Wolf"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nyxborn Wolf");
        harness.assertInGraveyard(player1, "Nyxborn Wolf");
        harness.assertOnBattlefield(player1, "Swordwise Centaur");
    }

    @Test
    @DisplayName("A bestowed target that becomes a creature before resolution is no longer legal")
    void targetLosingAuraSubtypeDoesNotResolve() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        harness.setHand(player1, List.of(new NyxbornWolf()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        UUID auraId = harness.getPermanentId(player1, "Nyxborn Wolf");
        harness.setHand(player1, List.of(new SetessanStarbreaker()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, auraId);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Nyxborn Wolf");
        harness.assertNotInGraveyard(player1, "Nyxborn Wolf");
    }
}
