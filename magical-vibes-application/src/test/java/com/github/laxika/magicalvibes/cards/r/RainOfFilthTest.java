package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RainOfFilth.class, Forest.class})
class RainOfFilthTest extends BaseCardTest {

    @Test
    @DisplayName("Lands you control gain a sacrifice-for-black-mana ability until end of turn")
    void grantsSacrificeForBlackManaAbility() {
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new RainOfFilth(), "{B}");
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The granted ability is removed at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new RainOfFilth(), "{B}");
        harness.passBothPriorities();

        assertThat(forest.getTemporaryActivatedAbilities()).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(forest.getTemporaryActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Grants the ability to every land you control, but not an opponent's land")
    void grantsToEveryLandControlledAtResolution() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.castFromHand(player1, new RainOfFilth(), "{B}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Forest"))
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Does not grant the ability to a land that enters after Rain of Filth resolves")
    void doesNotGrantAbilityToLaterLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new RainOfFilth(), "{B}");
        harness.passBothPriorities();

        Permanent laterForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        int laterForestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(laterForest);

        assertThatThrownBy(() -> harness.activateAbility(player1, laterForestIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Forest"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(laterForest);
    }

    @Test
    @DisplayName("The granted ability can be activated while the land is tapped")
    void grantedAbilityWorksWhileLandIsTapped() {
        harness.addToBattlefield(player1, new Forest());
        harness.tapPermanent(player1, 0);
        harness.castFromHand(player1, new RainOfFilth(), "{B}");
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Forest");
    }
}
