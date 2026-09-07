package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Terraformer.class, Forest.class})
class TerraformerTest extends BaseCardTest {

    @Test
    @DisplayName("Each land controlled by the ability's controller becomes the chosen type")
    void allControllerLandsBecomeChosenType() {
        harness.addToBattlefield(player1, new Terraformer());
        Permanent forestA = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent forestB = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");

        assertThat(forestA.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
        assertThat(forestB.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("The ability does not change an opponent's lands")
    void opponentLandsAreUnaffected() {
        harness.addToBattlefield(player1, new Terraformer());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");

        assertThat(opponentForest.getTransientLandTypeOverride()).isNull();
    }

    @Test
    @DisplayName("The chosen type replaces the lands' existing land types")
    void chosenTypeReplacesExistingTypes() {
        harness.addToBattlefield(player1, new Terraformer());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");

        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
        assertThat(forest.getTransientSubtypes()).isEmpty();
    }

    private void activateAndChoose(String subtype) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype);
    }
}
