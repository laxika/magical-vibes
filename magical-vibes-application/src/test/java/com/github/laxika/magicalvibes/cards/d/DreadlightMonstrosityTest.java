package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BleedDry;
import com.github.laxika.magicalvibes.cards.s.SelhoffEntomber;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreadlightMonstrosity.class, SelhoffEntomber.class, BleedDry.class})
class DreadlightMonstrosityTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate without owning a card in exile")
    void cannotActivateWithoutOwningCardInExile() {
        addCreatureReady(player1, new DreadlightMonstrosity());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("own a card in exile");
    }

    @Test
    @DisplayName("Own card in exile enables the unblockable ability")
    void ownCardInExileEnablesAbility() {
        Permanent monstrosity = addCreatureReady(player1, new DreadlightMonstrosity());
        harness.setExile(player1, List.of(new SelhoffEntomber()));
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(monstrosity.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("An opponent's exiled card does not satisfy the activation condition")
    void opponentsExiledCardDoesNotEnableAbility() {
        addCreatureReady(player1, new DreadlightMonstrosity());
        harness.setExile(player2, List.of(new SelhoffEntomber()));
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("own a card in exile");
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when its payment is declined")
    void wardCountersOpponentsSpell() {
        Permanent monstrosity = addCreatureReady(player1, new DreadlightMonstrosity());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BleedDry()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castInstant(player2, 0, monstrosity.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Dreadlight Monstrosity");
        harness.assertInGraveyard(player2, "Bleed Dry");
    }

    @Test
    @DisplayName("Paying ward allows an opponent's spell to resolve")
    void payingWardAllowsSpellToResolve() {
        Permanent monstrosity = addCreatureReady(player1, new DreadlightMonstrosity());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BleedDry()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castInstant(player2, 0, monstrosity.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Dreadlight Monstrosity");
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.ownerId()).isEqualTo(player1.getId());
            assertThat(entry.card().getId()).isEqualTo(monstrosity.getCard().getId());
        });
    }

    @Test
    @DisplayName("Ward does not trigger for the controller's own spell")
    void ownSpellDoesNotTriggerWard() {
        Permanent monstrosity = addCreatureReady(player1, new DreadlightMonstrosity());
        harness.setHand(player1, List.of(new BleedDry()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, monstrosity.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Dreadlight Monstrosity");
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.ownerId()).isEqualTo(player1.getId());
            assertThat(entry.card().getId()).isEqualTo(monstrosity.getCard().getId());
        });
    }

    @Test
    @DisplayName("The exile restriction is checked at activation, not resolution")
    void abilityResolvesAfterLastOwnedCardLeavesExile() {
        Permanent monstrosity = addCreatureReady(player1, new DreadlightMonstrosity());
        SelhoffEntomber exiledCard = new SelhoffEntomber();
        harness.setExile(player1, List.of(exiledCard));
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        gd.removeFromExile(exiledCard.getId());
        harness.setHand(player1, List.of(exiledCard));
        harness.passBothPriorities();

        assertThat(monstrosity.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockability expires at the end of the turn")
    void unblockabilityExpiresAtEndOfTurn() {
        Permanent monstrosity = addCreatureReady(player1, new DreadlightMonstrosity());
        harness.setExile(player1, List.of(new SelhoffEntomber()));
        harness.setHand(player1, List.of());
        addManaForAbility();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(monstrosity.isCantBeBlocked()).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(monstrosity.isCantBeBlocked()).isFalse();
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
