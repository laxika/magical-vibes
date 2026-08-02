package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EyeOfNowhereTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature to its owner's hand")
    void returnsCreatureToHand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EyeOfNowhere()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Eye of Nowhere");
    }

    @Test
    @DisplayName("Returns target enchantment to its owner's hand")
    void returnsEnchantmentToHand() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new EyeOfNowhere()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Angelic Chorus"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInHand(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Can return a land, including one the caster controls")
    void returnsOwnLandToHand() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new EyeOfNowhere()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player1, "Island"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EyeOfNowhere()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Eye of Nowhere");
    }
}
