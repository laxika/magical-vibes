package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NephaliaSmugglerTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers another creature you control and taps the Smuggler")
    void flickersOwnCreature() {
        harness.addToBattlefield(player1, new NephaliaSmuggler());
        findPermanent(player1, "Nephalia Smuggler").setSummoningSick(false);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        // Returned as a new object, so it is summoning sick again.
        assertThat(findPermanent(player1, "Grizzly Bears").isSummoningSick()).isTrue();
        assertThat(findPermanent(player1, "Nephalia Smuggler").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        harness.addToBattlefield(player1, new NephaliaSmuggler());
        findPermanent(player1, "Nephalia Smuggler").setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID smugglerId = harness.getPermanentId(player1, "Nephalia Smuggler");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, smugglerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new NephaliaSmuggler());
        findPermanent(player1, "Nephalia Smuggler").setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new NephaliaSmuggler());
        findPermanent(player1, "Nephalia Smuggler").setSummoningSick(false);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);

        Permanent bears = gqs.findPermanentById(gd, bearsId);
        gd.playerBattlefields.get(player1.getId()).remove(bears);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
