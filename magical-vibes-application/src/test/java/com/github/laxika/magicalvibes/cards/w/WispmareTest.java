package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WispmareTest extends BaseCardTest {

    // ===== Hardcast =====

    @Test
    @DisplayName("Hardcast: ETB destroys target enchantment and Wispmare stays on the battlefield")
    void hardcastDestroysEnchantmentAndStays() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player1, "Wispmare");
    }

    // ===== Evoke =====

    @Test
    @DisplayName("Evoke: paying only {W}, ETB still destroys the target enchantment")
    void evokeDestroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castCreatureWithEvoke(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger (destroy + evoke sacrifice)

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Angelic Chorus");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Evoke: Wispmare is sacrificed as it enters")
    void evokeSacrificesSelf() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
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
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() ->
                harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, creatureId, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
