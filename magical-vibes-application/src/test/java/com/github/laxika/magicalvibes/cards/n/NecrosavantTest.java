package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.p.PantherWarriors;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Necrosavant")
@CardUsed({Necrosavant.class, PantherWarriors.class})
class NecrosavantTest extends BaseCardTest {

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Activating during upkeep sacrifices a creature and resolves it back to the battlefield")
    void activatingReturnsToBattlefield() {
        advanceToUpkeep(player1);
        harness.setGraveyard(player1, List.of(new Necrosavant()));
        harness.addToBattlefield(player1, new PantherWarriors());
        addMana(player1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        // Panther Warriors was sacrificed to pay the cost
        harness.assertNotOnBattlefield(player1, "Panther Warriors");

        // Necrosavant is on the battlefield untapped and no longer in the graveyard
        Permanent perm = findPermanent(player1, "Necrosavant");
        assertThat(perm.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Necrosavant");
    }

    @Test
    @DisplayName("Cannot activate outside of your upkeep")
    void cannotActivateOutsideUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Necrosavant()));
        harness.addToBattlefield(player1, new PantherWarriors());
        addMana(player1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreatureToSacrifice() {
        advanceToUpkeep(player1);
        harness.setGraveyard(player1, List.of(new Necrosavant()));
        addMana(player1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate during an opponent's upkeep")
    void cannotActivateDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setGraveyard(player1, List.of(new Necrosavant()));
        harness.addToBattlefield(player1, new PantherWarriors());
        addMana(player1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with the right total mana but not enough black mana")
    void cannotActivateWithoutRequiredColoredMana() {
        advanceToUpkeep(player1);
        harness.setGraveyard(player1, List.of(new Necrosavant()));
        harness.addToBattlefield(player1, new PantherWarriors());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice cost chooses one creature when several are available")
    void sacrificeCostChoosesOneCreature() {
        advanceToUpkeep(player1);
        harness.setGraveyard(player1, List.of(new Necrosavant()));
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new PantherWarriors());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new PantherWarriors());
        addMana(player1);

        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificed).contains(survivor);
        harness.assertOnBattlefield(player1, "Necrosavant");
    }

    @Test
    @DisplayName("Returns only the activated card when another copy is in the graveyard")
    void returnsOnlyActivatedCard() {
        advanceToUpkeep(player1);
        Necrosavant activatedCard = new Necrosavant();
        Necrosavant otherCopy = new Necrosavant();
        harness.setGraveyard(player1, List.of(activatedCard, otherCopy));
        harness.addToBattlefield(player1, new PantherWarriors());
        addMana(player1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(activatedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(otherCopy)
                .noneMatch(card -> card.getId().equals(activatedCard.getId()));
    }
}
