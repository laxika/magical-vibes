package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.cards.h.HundredTalonKami;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyeOfNowhere.class, HundredTalonKami.class, HondenOfSeeingWinds.class, Island.class})
class EyeOfNowhereTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature to its owner's hand")
    void returnsCreatureToHand() {
        harness.addToBattlefield(player2, new HundredTalonKami());
        harness.setHand(player1, List.of(new EyeOfNowhere()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Hundred-Talon Kami"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hundred-Talon Kami");
        harness.assertInHand(player2, "Hundred-Talon Kami");
        harness.assertInGraveyard(player1, "Eye of Nowhere");
    }

    @Test
    @DisplayName("Returns target enchantment to its owner's hand")
    void returnsEnchantmentToHand() {
        harness.addToBattlefield(player2, new HondenOfSeeingWinds());
        harness.setHand(player1, List.of(new EyeOfNowhere()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Honden of Seeing Winds"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Honden of Seeing Winds");
        harness.assertInHand(player2, "Honden of Seeing Winds");
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
    @DisplayName("Returns a permanent to its owner's hand when controlled by another player")
    void returnsTargetToItsOwnersHand() {
        var island = new Island();
        island.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, island);
        harness.setHand(player1, List.of(new EyeOfNowhere()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player1, "Island"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player2, "Island");
        harness.assertNotInHand(player1, "Island");
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        var target = harness.addToBattlefieldAndReturn(player2, new HundredTalonKami());
        harness.setHand(player1, List.of(new EyeOfNowhere()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(harness.getGameData(), target));
        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInHand(player2, "Hundred-Talon Kami");
        harness.assertInGraveyard(player1, "Eye of Nowhere");
    }
}
