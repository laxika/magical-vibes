package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({IllustriousHistorian.class})
class IllustriousHistorianTest extends BaseCardTest {

    private void setUpAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new IllustriousHistorian()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    @Test
    @DisplayName("Ability exiles the source card from the graveyard as a cost")
    void abilityExilesSourceAsCost() {
        setUpAbility();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Illustrious Historian");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Illustrious Historian"));
    }

    @Test
    @DisplayName("Resolving ability creates a tapped 3/2 red and white Spirit token")
    void resolvingCreatesTappedSpiritToken() {
        setUpAbility();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Spirit");

        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(token.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability can be activated during the opponent's turn")
    void canActivateDuringOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new IllustriousHistorian()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Illustrious Historian");
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(countPermanents(player2, "Spirit")).isZero();
    }

    @Test
    @DisplayName("Ability can be activated during combat")
    void canActivateDuringCombat() {
        setUpAbility();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(findPermanent(player1, "Spirit").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another copy can be activated in response to the first ability")
    void canActivateWithNonemptyStack() {
        setUpAbility();
        harness.setGraveyard(player1, List.of(new IllustriousHistorian(), new IllustriousHistorian()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0);
        assertThat(countPermanents(player1, "Spirit")).isZero();
        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Illustrious Historian");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
        assertThat(findPermanents(player1, "Spirit")).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Insufficient mana does not exile the source or create a token")
    void cannotActivateWithoutFiveMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new IllustriousHistorian()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Illustrious Historian");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
