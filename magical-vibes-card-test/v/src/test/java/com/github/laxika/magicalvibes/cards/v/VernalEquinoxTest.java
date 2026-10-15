package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FoodChain;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.t.Tranquility;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VernalEquinox.class, FreshVolunteers.class, FoodChain.class, Tranquility.class})
class VernalEquinoxTest extends BaseCardTest {

    @Test
    @DisplayName("The controller can cast creature spells at instant speed")
    void controllerCanCastCreatureAtInstantSpeed() {
        harness.addToBattlefield(player1, new VernalEquinox());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new FreshVolunteers(), "{1}{W}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The controller can cast enchantment spells at instant speed")
    void controllerCanCastEnchantmentAtInstantSpeed() {
        harness.addToBattlefield(player1, new VernalEquinox());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new FoodChain(), "{2}{G}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Any player can cast a creature spell at instant speed")
    void opponentCanCastCreatureAtInstantSpeed() {
        harness.addToBattlefield(player1, new VernalEquinox());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new FreshVolunteers(), "{1}{W}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Any player can cast an enchantment spell at instant speed")
    void opponentCanCastEnchantmentAtInstantSpeed() {
        harness.addToBattlefield(player1, new VernalEquinox());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new FoodChain(), "{2}{G}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Sorcery spells do not gain flash")
    void sorceryDoesNotGainFlash() {
        harness.addToBattlefield(player1, new VernalEquinox());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Tranquility()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Creature and enchantment spells can be cast in response to another spell")
    void canCastInResponseToAnotherSpell() {
        harness.addToBattlefield(player1, new VernalEquinox());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Tranquility(), "{2}{G}");

        harness.castFromHand(player2, new FreshVolunteers(), "{1}{W}");
        harness.castFromHand(player2, new FoodChain(), "{2}{G}");

        assertThat(gd.stack).hasSize(3);
    }

    @Test
    @DisplayName("The flash permission ends when Vernal Equinox leaves the battlefield")
    void permissionEndsWhenSourceLeaves() {
        harness.addToBattlefield(player1, new VernalEquinox());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Vernal Equinox");
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player1, new FreshVolunteers(), "{1}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThatThrownBy(() -> harness.castFromHand(player2, new FoodChain(), "{2}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Vernal Equinox in hand does not grant flash to itself or creatures")
    void sourceInHandDoesNotGrantFlash() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VernalEquinox(), new FreshVolunteers()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThatThrownBy(() -> harness.castCreature(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.stack).isEmpty();
    }
}
