package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdarkarValkyrieTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a targeted creature under the Valkyrie's controller's control when it dies this turn")
    void returnsTargetedCreatureUnderAbilityControllersControl() {
        Permanent valkyrie = addReadyValkyrie();
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(valkyrie.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target the Valkyrie itself")
    void cannotTargetItself() {
        Permanent valkyrie = addReadyValkyrie();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, valkyrie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyValkyrie() {
        Permanent valkyrie = new Permanent(new AdarkarValkyrie());
        valkyrie.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(valkyrie);
        return valkyrie;
    }
}
