package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrcishLumberjackTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Forest adds three mana in the chosen combination of {R} and/or {G}")
    void producesThreeChosenMana() {
        String[][] combos = {{"RED", "RED", "RED"}, {"RED", "GREEN", "GREEN"}, {"GREEN", "GREEN", "GREEN"}};
        for (String[] combo : combos) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();
            gd = harness.getGameData();

            addReadyLumberjack(player1);
            harness.addToBattlefield(player1, new Forest());

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, combo[0]);
            harness.handleListChoice(player1, combo[1]);
            harness.handleListChoice(player1, combo[2]);

            long expectedRed = java.util.Arrays.stream(combo).filter("RED"::equals).count();
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo((int) expectedRed);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3 - (int) expectedRed);
            assertThat(gd.stack).isEmpty();
            harness.assertInGraveyard(player1, "Forest");
        }
    }

    @Test
    @DisplayName("Cannot be activated with no Forest to sacrifice")
    void requiresAForest() {
        addReadyLumberjack(player1);
        harness.addToBattlefield(player1, new Mountain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyLumberjack(Player player) {
        Permanent perm = new Permanent(new OrcishLumberjack());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
