package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GravestoneStrider.class, GrizzlyBears.class})
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
        Card target = new GrizzlyBears();
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
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(gd.playerBattlefields.get(player2.getId()).getFirst().getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
