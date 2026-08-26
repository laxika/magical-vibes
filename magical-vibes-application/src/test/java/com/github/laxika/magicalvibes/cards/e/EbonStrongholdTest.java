package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EbonStronghold.class})
class EbonStrongholdTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new EbonStronghold()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one black mana")
    void tapAddsOneBlackMana() {
        harness.addToBattlefield(player1, new EbonStronghold());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(findPermanent(player1, "Ebon Stronghold").isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Ebon Stronghold");
    }

    @Test
    @DisplayName("Cannot activate the sacrifice ability after tapping for one black mana")
    void cannotSacrificeAfterTappingForMana() {
        harness.addToBattlefield(player1, new EbonStronghold());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ebon Stronghold");
    }

    @Test
    @DisplayName("Tap and sacrifice adds two black mana and moves the land to the graveyard")
    void sacrificeAddsTwoBlackMana() {
        harness.addToBattlefield(player1, new EbonStronghold());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Ebon Stronghold");
        harness.assertInGraveyard(player1, "Ebon Stronghold");
    }
}
