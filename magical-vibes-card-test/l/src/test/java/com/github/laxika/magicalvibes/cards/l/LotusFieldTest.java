package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AgentOfTreachery;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LotusField.class, Forest.class, Island.class, AgentOfTreachery.class})
class LotusFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices two lands of the controller's choice")
    void enterSacrificesTwoLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new LotusField()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId(), island.getId()));

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Lotus Field");
        assertThat(findPermanent(player1, "Lotus Field").isTapped()).isTrue();
    }

    @Test
    @DisplayName("With no other lands, Lotus Field sacrifices itself")
    void sacrificesItselfWhenItIsTheOnlyLand() {
        harness.setHand(player1, List.of(new LotusField()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lotus Field");
        harness.assertInGraveyard(player1, "Lotus Field");
    }

    @Test
    void sacrificesBothLandsWhenOnlyOneOtherLandExists() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new LotusField()));

        harness.playLand(player1, 0);
        assertThat(findPermanent(player1, "Lotus Field").isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Lotus Field");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    void controllerCanChooseToSacrificeLotusField() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new LotusField()));

        harness.playLand(player1, 0);
        Permanent lotus = findPermanent(player1, "Lotus Field");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId(), lotus.getId()));

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Lotus Field");
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    void opponentCannotTargetLotusFieldWithAnEnterBattlefieldAbility() {
        Permanent lotus = harness.addToBattlefieldAndReturn(player2, new LotusField());
        harness.setHand(player1, List.of(new AgentOfTreachery()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, lotus.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Lotus Field");
    }

    @ParameterizedTest
    @ValueSource(strings = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Tapping Lotus Field adds three mana of the chosen color")
    void manaAbilityAddsThreeManaOfChosenColor(String color) {
        harness.addToBattlefield(player1, new LotusField());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, color);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor == ManaColor.valueOf(color) ? 3 : 0);
        }
    }
}
