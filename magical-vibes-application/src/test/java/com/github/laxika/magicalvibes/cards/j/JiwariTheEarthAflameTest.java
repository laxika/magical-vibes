package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JiwariTheEarthAflameTest extends BaseCardTest {

    @Test
    void battlefieldAbilityDealsXDamageToTargetCreatureWithoutFlying() {
        Permanent jiwari = addCreatureReady(player1, new JiwariTheEarthAflame());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(jiwari.isTapped()).isTrue();
    }

    @Test
    void channelDealsXDamageToEachCreatureWithoutFlyingAndDiscardsSource() {
        harness.setHand(player1, List.of(new JiwariTheEarthAflame()));
        addCreatureReady(player2, new GrizzlyBears());
        Permanent flying = addCreatureReady(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null, 2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jiwari, the Earth Aflame");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    void battlefieldAbilityCannotTargetCreatureWithFlying() {
        addCreatureReady(player1, new JiwariTheEarthAflame());
        Permanent target = addCreatureReady(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature without flying");
    }
}
