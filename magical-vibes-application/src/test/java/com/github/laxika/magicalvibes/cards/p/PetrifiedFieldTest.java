package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.r.RavagedHighlands;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PetrifiedField.class, RavagedHighlands.class, DuskImp.class})
class PetrifiedFieldTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C}")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new PetrifiedField());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("{T}, Sacrifice this land returns a target land card from the graveyard to hand")
    void sacrificesAndReturnsTargetLand() {
        harness.addToBattlefield(player1, new PetrifiedField());

        Card land = new RavagedHighlands();
        harness.setGraveyard(player1, List.of(land));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ravaged Highlands");
        harness.assertNotInGraveyard(player1, "Ravaged Highlands");
        harness.assertInGraveyard(player1, "Petrified Field");
        harness.assertNotOnBattlefield(player1, "Petrified Field");
    }

    @Test
    @DisplayName("The ability cannot target a nonland card")
    void cannotTargetNonlandCard() {
        harness.addToBattlefield(player1, new PetrifiedField());

        Card creature = new DuskImp();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Petrified Field");
    }

    @Test
    @DisplayName("The ability cannot target a land card in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        harness.addToBattlefield(player1, new PetrifiedField());

        Card opponentLand = new RavagedHighlands();
        harness.setGraveyard(player2, List.of(opponentLand));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(opponentLand.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Petrified Field");
    }
}
