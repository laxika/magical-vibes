package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScionOfDarkness.class, GrizzlyBears.class, HolyDay.class})
class ScionOfDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage reanimates a creature from the damaged player's graveyard")
    void combatDamageReanimatesChosenCreature() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        attackWithScionDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("The optional reanimation may be declined")
    void reanimationMayBeDeclined() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        attackWithScionDealingDamage();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("Only creature cards from the damaged player's graveyard are offered")
    void onlyDamagedPlayersCreatureCardsAreOffered() {
        Card creature = new GrizzlyBears();
        Card instant = new HolyDay();
        harness.setGraveyard(player2, List.of(creature, instant));

        attackWithScionDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Cycling discards Scion of Darkness and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ScionOfDarkness()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Scion of Darkness");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scion of Darkness");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The trigger must choose a target even when its controller intends to decline")
    void targetIsRequiredBeforeTheOptionalResolutionChoice() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        attackWithScionDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target that leaves the graveyard before resolution is not reanimated")
    void targetLeavingGraveyardMakesTriggerIneffective() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        attackWithScionDealingDamage();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(bears));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(bears.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Creature cards in the controller's graveyard are not eligible")
    void controllersGraveyardIsExcluded() {
        Card ownCreature = new ScionOfDarkness();
        Card opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));

        attackWithScionDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(opposingCreature.getId());
    }

    @Test
    @DisplayName("Trample allows targeting the blocker killed by the same combat damage")
    void trampleCanReanimateTheJustKilledBlocker() {
        Card bears = new GrizzlyBears();
        addCreatureReady(player1, new ScionOfDarkness());
        Permanent blocker = addCreatureReady(player2, bears);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 4));

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Grizzly Bears").isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Prevented combat damage does not trigger reanimation")
    void preventedCombatDamageDoesNotTrigger() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new HolyDay()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0);

        attackWithScionDealingDamage();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated without paying three mana")
    void cyclingRequiresThreeMana() {
        harness.setHand(player1, List.of(new ScionOfDarkness()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Scion of Darkness");
        harness.assertNotInGraveyard(player1, "Scion of Darkness");
        assertThat(gd.stack).isEmpty();
    }

    private void attackWithScionDealingDamage() {
        addCreatureReady(player1, new ScionOfDarkness()).setAttacking(true);
        resolveCombat();
    }
}
