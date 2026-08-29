package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Spirit"))
                .findFirst().orElseThrow();

        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(token.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability can only be activated at sorcery speed")
    void onlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new IllustriousHistorian()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Illustrious Historian");
    }
}
