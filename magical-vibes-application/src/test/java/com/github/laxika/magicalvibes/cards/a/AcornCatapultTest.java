package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcornCatapult.class, GrizzlyBears.class})
class AcornCatapultTest extends BaseCardTest {

    @Test
    @DisplayName("Target player takes 1 damage and creates a Squirrel")
    void targetPlayerCreatesSquirrel() {
        addReadyCatapult(player1);
        harness.setLife(player2, 20);
        addMana(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(findSquirrels(player1)).isEmpty();
        assertThat(findSquirrels(player2)).hasSize(1);
    }

    @Test
    @DisplayName("Target permanent's controller takes 1 damage and creates a Squirrel")
    void targetPermanentControllerCreatesSquirrel() {
        addReadyCatapult(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addMana(player1);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getMarkedDamage()).isEqualTo(1);
        assertThat(findSquirrels(player1)).isEmpty();
        assertThat(findSquirrels(player2)).hasSize(1);
    }

    private Permanent addReadyCatapult(Player player) {
        Permanent catapult = harness.addToBattlefieldAndReturn(player, new AcornCatapult());
        catapult.setSummoningSick(false);
        return catapult;
    }

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private List<Permanent> findSquirrels(Player player) {
        return findPermanents(player, "Squirrel");
    }
}
