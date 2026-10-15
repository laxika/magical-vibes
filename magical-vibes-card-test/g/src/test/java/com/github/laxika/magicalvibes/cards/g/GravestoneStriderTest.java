package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GravestoneStrider.class})
class GravestoneStriderTest extends BaseCardTest {

    @Test
    @DisplayName("The mana ability adds one mana of the chosen color")
    void manaAbilityAddsChosenColor() {
        harness.addToBattlefieldAndReturn(player1, new GravestoneStrider());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("The mana ability can be activated only once each turn")
    void manaAbilityIsLimitedToOnceEachTurn() {
        harness.addToBattlefieldAndReturn(player1, new GravestoneStrider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The graveyard ability exiles itself as a cost and the target card on resolution")
    void graveyardAbilityExilesSourceAndTarget() {
        Card strider = new GravestoneStrider();
        Card target = new GravestoneStrider();
        harness.setGraveyard(player1, List.of(strider));
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(strider);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("The graveyard ability requires a card in a graveyard as its target")
    void graveyardAbilityRejectsNonGraveyardTarget() {
        Card strider = new GravestoneStrider();
        harness.setGraveyard(player1, List.of(strider));
        harness.addToBattlefieldAndReturn(player2, new GravestoneStrider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(gd.playerBattlefields.get(player2.getId()).getFirst().getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void graveyardAbilityPaysCostsBeforeResolvingAndCanTargetOwnGraveyard() {
        Card source = new GravestoneStrider();
        Card target = new GravestoneStrider();
        harness.setGraveyard(player1, List.of(source, target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(source);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(source, target);
    }

    @Test
    void graveyardAbilityCanTargetItselfBeforePayingExileCost() {
        Card source = new GravestoneStrider();
        harness.setGraveyard(player1, List.of(source));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(source.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(source);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(source);
    }

    @Test
    void graveyardAbilityRejectsMissingTargetWithoutPayingCosts() {
        Card source = new GravestoneStrider();
        harness.setGraveyard(player1, List.of(source));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void separatePermanentsCanEachActivateTheirManaAbility() {
        harness.addToBattlefield(player1, new GravestoneStrider());
        harness.addToBattlefield(player1, new GravestoneStrider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manaAbilityCanActivateAgainDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new GravestoneStrider());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
