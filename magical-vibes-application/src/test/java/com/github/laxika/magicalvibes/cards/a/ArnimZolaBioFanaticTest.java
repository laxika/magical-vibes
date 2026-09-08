package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArnimZolaBioFanatic.class, GrizzlyBears.class})
class ArnimZolaBioFanaticTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate without two creature cards in the graveyard")
    void cannotActivateWithoutTwoCreatureCardsInGraveyard() {
        Permanent arnim = addCreatureReady(player1, new ArnimZolaBioFanatic());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("2 or more creature cards");
        assertThat(arnim.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creates a tapped 2/1 Villain token with menace")
    void createsTappedVillainTokenWithMenace() {
        addCreatureReady(player1, new ArnimZolaBioFanatic());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.isTapped()).isTrue();
        assertThat(token.hasKeyword(Keyword.MENACE)).isTrue();
    }
}
