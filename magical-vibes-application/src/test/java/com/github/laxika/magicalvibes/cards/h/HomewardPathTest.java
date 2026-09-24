package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HomewardPath.class, GrizzlyBears.class})
class HomewardPathTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds {C}")
    void tapForColorlessMana() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new HomewardPath());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(path.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping returns creatures to their owners' control")
    void returnsCreaturesToTheirOwnersControl() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new HomewardPath());
        Permanent playerOneCreature = addStolenCreature(player1, player2);
        Permanent playerTwoCreature = addStolenCreature(player2, player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(path.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(playerOneCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(playerTwoCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(playerTwoCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(playerOneCreature);
    }

    private Permanent addStolenCreature(Player owner, Player controller) {
        Permanent creature = harness.addToBattlefieldAndReturn(owner, new GrizzlyBears());
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, controller.getId(), creature,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));
        return creature;
    }
}
