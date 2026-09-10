package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinFirebug.class, Forest.class, Shock.class})
class GoblinFirebugTest extends BaseCardTest {

    @Test
    @DisplayName("When Goblin Firebug leaves the battlefield, its controller sacrifices a land")
    void sacrificesLandWhenLeavingBattlefield() {
        Permanent firebug = addCreatureReady(player1, new GoblinFirebug());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, firebug.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Goblin Firebug");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The leave trigger does nothing when its controller controls no lands")
    void doesNothingWithoutAControllerLand() {
        Permanent firebug = addCreatureReady(player1, new GoblinFirebug());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, firebug.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Goblin Firebug");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest"));
    }
}
