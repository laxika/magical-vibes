package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GazeOfTheGorgon.class, GiantSpider.class, GrizzlyBears.class, Forest.class})
class GazeOfTheGorgonTest extends BaseCardTest {

    @Test
    @DisplayName("Regenerates the target and destroys its combat opponents at end of combat")
    void regeneratesTargetAndDestroysCombatOpponents() {
        Permanent target = addReady(player1, new GiantSpider());
        target.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        castGaze(player1, target);
        resolveAllTriggers();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A noncreature permanent cannot be targeted")
    void cannotTargetNonCreature() {
        Permanent land = addReady(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GazeOfTheGorgon()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGaze(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new GazeOfTheGorgon()));
        harness.addMana(caster, ManaColor.GREEN, 4);
        harness.castInstant(caster, 0, target.getId());
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
