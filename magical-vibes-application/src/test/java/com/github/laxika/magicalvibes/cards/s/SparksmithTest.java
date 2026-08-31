package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sparksmith.class, RagingGoblin.class, GrizzlyBears.class})
class SparksmithTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the Goblins on the battlefield and damages its controller")
    void dealsDamageEqualToBattlefieldGoblinsAndDamagesController() {
        harness.setLife(player1, 20);
        Permanent sparksmith = addReadySparksmith(player1);
        harness.addToBattlefield(player2, new RagingGoblin());
        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(sparksmith.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counts Goblins when the ability resolves")
    void countsGoblinsAtResolution() {
        harness.setLife(player1, 20);
        addReadySparksmith(player1);
        harness.addToBattlefield(player2, new RagingGoblin());
        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Raging Goblin"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetPlayer() {
        addReadySparksmith(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySparksmith(Player player) {
        return addReadyCreature(player, new Sparksmith());
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
