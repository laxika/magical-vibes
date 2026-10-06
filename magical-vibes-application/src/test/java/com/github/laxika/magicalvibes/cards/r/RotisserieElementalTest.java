package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RotisserieElemental.class})
class RotisserieElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage adds a skewer counter and offers the sacrifice")
    void combatDamageAddsCounterAndOffersSacrifice() {
        Permanent elemental = addReadyElemental();
        elemental.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.SKEWER)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Sacrificing exiles cards equal to the updated skewer counter count")
    void sacrificeExilesCardsEqualToCounterCount() {
        Card first = new RotisserieElemental();
        Card second = new RotisserieElemental();
        Card third = new RotisserieElemental();
        harness.setLibrary(player1, List.of(first, second, third));

        Permanent elemental = addReadyElemental();
        elemental.setCounterCount(CounterType.SKEWER, 2);
        elemental.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(elemental.getCounterCount(CounterType.SKEWER)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elemental);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elemental.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .containsEntry(third.getId(), player1.getId());
    }

    @Test
    @DisplayName("Declining the sacrifice leaves the elemental and library unchanged")
    void decliningSacrificeDoesNothingAfterAddingCounter() {
        Card first = new RotisserieElemental();
        harness.setLibrary(player1, List.of(first));

        Permanent elemental = addReadyElemental();
        elemental.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(elemental.getCounterCount(CounterType.SKEWER)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(first);
    }

    @Test
    void sacrificeExilesOnlyAvailableCardsFromShortLibrary() {
        Card top = new RotisserieElemental();
        harness.setLibrary(player1, List.of(top));
        Permanent elemental = addReadyElemental();
        elemental.setCounterCount(CounterType.SKEWER, 4);
        elemental.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elemental.getCard());
    }

    @Test
    void sourceLeavingBeforeResolutionPreventsExilingCards() {
        Card top = new RotisserieElemental();
        harness.setLibrary(player1, List.of(top));
        Permanent elemental = addReadyElemental();
        elemental.setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).isNotEmpty();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, elemental));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elemental.getCard());
    }

    @Test
    void exiledCreatureRequiresMainPhaseAndPaymentOfItsManaCost() {
        Card top = new RotisserieElemental();
        harness.setLibrary(player1, List.of(top));
        Permanent elemental = addReadyElemental();
        elemental.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gd.playerManaPools.get(player1.getId()).clear();
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, top.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(top.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void playPermissionExpiresAtCleanupButCardRemainsExiled() {
        Card top = new RotisserieElemental();
        harness.setLibrary(player1, List.of(top));
        Permanent elemental = addReadyElemental();
        elemental.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.inMutationScope(() -> GameTestEngineContext.get()
                .getBean(TurnCleanupService.class).applyCleanupResets(gd));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    private Permanent addReadyElemental() {
        return addCreatureReady(player1, new RotisserieElemental());
    }
}
