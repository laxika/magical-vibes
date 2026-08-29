package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(HavenwoodBattleground.class)
class HavenwoodBattlegroundTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new HavenwoodBattleground()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Havenwood Battleground").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one green mana")
    void tapAddsOneGreenMana() {
        harness.addToBattlefield(player1, new HavenwoodBattleground());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Havenwood Battleground");
    }

    @Test
    @DisplayName("Tap ability cannot be activated again while the land is tapped")
    void tapAbilityRequiresUntappedLand() {
        harness.addToBattlefield(player1, new HavenwoodBattleground());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Tap and sacrifice adds two green mana and moves the land to the graveyard")
    void sacrificeAddsTwoGreenMana() {
        harness.addToBattlefield(player1, new HavenwoodBattleground());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Havenwood Battleground");
        harness.assertInGraveyard(player1, "Havenwood Battleground");
    }
}
