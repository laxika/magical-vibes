package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScourgeOfNelToth.class, GrizzlyBears.class})
class ScourgeOfNelTothTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast from the graveyard by paying black mana and sacrificing two creatures")
    void castsFromGraveyardBySacrificingTwoCreatures() {
        Permanent firstBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        ScourgeOfNelToth scourge = new ScourgeOfNelToth();
        harness.setGraveyard(player1, List.of(scourge));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castFromGraveyardWithSacrifices(player1, 0, null,
                List.of(firstBears.getId(), secondBears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scourge of Nel Toth");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Requires exactly two creatures for the graveyard cast")
    void requiresTwoCreaturesForGraveyardCast() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        ScourgeOfNelToth scourge = new ScourgeOfNelToth();
        harness.setGraveyard(player1, List.of(scourge));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(
                player1, 0, null, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice exactly 2");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Scourge of Nel Toth");
    }

    @Test
    @DisplayName("Cannot sacrifice the same creature twice")
    void cannotSacrificeSameCreatureTwice() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        ScourgeOfNelToth scourge = new ScourgeOfNelToth();
        harness.setGraveyard(player1, List.of(scourge));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(
                player1, 0, null, List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate permanents");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
