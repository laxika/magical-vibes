package com.github.laxika.magicalvibes.cards.g;

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

class GoldmeadowNomadTest extends BaseCardTest {

    private void setUpAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new GoldmeadowNomad()));
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Test
    @DisplayName("Ability exiles the source card from the graveyard as a cost")
    void abilityExilesSourceAsCost() {
        setUpAbility();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Goldmeadow Nomad");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Goldmeadow Nomad"));
    }

    @Test
    @DisplayName("Resolving ability creates a 1/1 green and white Kithkin token")
    void resolvingCreatesKithkinToken() {
        setUpAbility();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Kithkin"))
                .findFirst().orElseThrow();

        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.KITHKIN);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
    }

    @Test
    @DisplayName("Ability can only be activated at sorcery speed")
    void onlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new GoldmeadowNomad()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Goldmeadow Nomad");
    }
}
