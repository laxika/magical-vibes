package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArashiTheSkyAsunderTest extends BaseCardTest {

    @Test
    void battlefieldAbilityDealsXDamageToTargetCreatureWithFlying() {
        Permanent arashi = addCreatureReady(player1, new ArashiTheSkyAsunder());
        Permanent target = addCreatureReady(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suntail Hawk");
        assertThat(arashi.isTapped()).isTrue();
    }

    @Test
    void channelDealsXDamageToEachCreatureWithFlyingAndDiscardsSource() {
        harness.setHand(player1, List.of(new ArashiTheSkyAsunder()));
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateHandAbility(player1, 0, null, 2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Arashi, the Sky Asunder");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    void battlefieldAbilityCannotTargetCreatureWithoutFlying() {
        addCreatureReady(player1, new ArashiTheSkyAsunder());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with flying");
    }
}
