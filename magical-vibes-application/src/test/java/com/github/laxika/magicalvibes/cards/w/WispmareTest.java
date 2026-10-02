package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.h.HoofprintsOfTheStag;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Wispmare.class, HoofprintsOfTheStag.class, HillcomberGiant.class})
class WispmareTest extends BaseCardTest {

    // ===== Hardcast =====

    @Test
    @DisplayName("Hardcast: ETB destroys target enchantment and Wispmare stays on the battlefield")
    void hardcastDestroysEnchantmentAndStays() {
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Hoofprints of the Stag");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Hoofprints of the Stag");
        harness.assertInGraveyard(player2, "Hoofprints of the Stag");
        harness.assertOnBattlefield(player1, "Wispmare");
    }

    @Test
    @DisplayName("ETB can target an enchantment controlled by Wispmare's controller")
    void canTargetOwnEnchantment() {
        harness.addToBattlefield(player1, new HoofprintsOfTheStag());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Hoofprints of the Stag");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hoofprints of the Stag");
        harness.assertInGraveyard(player1, "Hoofprints of the Stag");
        harness.assertOnBattlefield(player1, "Wispmare");
    }

    // ===== Evoke =====

    @Test
    @DisplayName("Evoke: paying only {W}, ETB still destroys the target enchantment")
    void evokeDestroysEnchantment() {
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Hoofprints of the Stag");
        harness.castCreatureWithEvoke(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger (destroy + evoke sacrifice)

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Hoofprints of the Stag");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Evoke: Wispmare is sacrificed as it enters")
    void evokeSacrificesSelf() {
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Hoofprints of the Stag");
        harness.castCreatureWithEvoke(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wispmare");
        harness.assertInGraveyard(player1, "Wispmare");
    }

    // ===== Illegal target =====

    @Test
    @DisplayName("Cannot target a creature with Wispmare's ETB")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new HillcomberGiant());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID creatureId = harness.getPermanentId(player2, "Hillcomber Giant");
        assertThatThrownBy(() ->
                harness.castCreature(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
