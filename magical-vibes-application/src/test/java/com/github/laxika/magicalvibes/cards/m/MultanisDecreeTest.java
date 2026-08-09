package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MultanisDecreeTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    @Test
    @DisplayName("Destroys all enchantments and gains 2 life per destroyed enchantment")
    void destroysAllEnchantmentsAndGainsLifePerDestroyedEnchantment() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.addToBattlefield(player2, new RuleOfLaw());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MultanisDecree()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Rule of Law");
        harness.assertNotOnBattlefield(player2, "Rule of Law");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 4);
    }

    @Test
    @DisplayName("Gains no life when no enchantments are destroyed")
    void gainsNoLifeWhenNoEnchantmentsAreDestroyed() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MultanisDecree()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
