package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LiegeOfTheHollows;
import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
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

@CardUsed({InfernalTribute.class, LiegeOfTheHollows.class, RedwoodTreefolk.class})
class InfernalTributeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a nontoken permanent draws a card")
    void sacrificeDrawsACard() {
        harness.addToBattlefield(player1, new InfernalTribute());
        harness.addToBattlefield(player1, new RedwoodTreefolk());
        harness.setLibrary(player1, List.of(new RedwoodTreefolk(), new RedwoodTreefolk()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Redwood Treefolk"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Redwood Treefolk");
        harness.assertNotOnBattlefield(player1, "Redwood Treefolk");
        harness.assertInGraveyard(player1, "Redwood Treefolk");
    }

    @Test
    @DisplayName("Infernal Tribute itself may be sacrificed to its own ability")
    void canSacrificeItself() {
        harness.addToBattlefield(player1, new InfernalTribute());
        harness.setLibrary(player1, List.of(new RedwoodTreefolk(), new RedwoodTreefolk()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Redwood Treefolk");
        harness.assertNotOnBattlefield(player1, "Infernal Tribute");
        harness.assertInGraveyard(player1, "Infernal Tribute");
    }

    @Test
    @DisplayName("Tokens are not legal sacrifices")
    void tokensCannotBeSacrificed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new InfernalTribute());
        harness.addToBattlefield(player1, new LiegeOfTheHollows());
        harness.addToBattlefield(player1, new RedwoodTreefolk());
        harness.setLibrary(player1, List.of(new RedwoodTreefolk(), new RedwoodTreefolk()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Liege of the Hollows"));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);

        List<Permanent> squirrels = findPermanents(player1, "Squirrel");
        assertThat(squirrels).hasSize(1);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .doesNotContain(squirrels.getFirst().getId())
                .contains(
                        harness.getPermanentId(player1, "Infernal Tribute"));
    }

    @Test
    @DisplayName("Ability cannot be activated without paying the mana cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new InfernalTribute());
        harness.addToBattlefield(player1, new RedwoodTreefolk());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
