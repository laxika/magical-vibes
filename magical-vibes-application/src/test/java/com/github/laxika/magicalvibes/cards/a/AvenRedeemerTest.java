package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenRedeemer.class, GrizzlyBears.class, Plains.class, Shock.class})
class AvenRedeemerTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 2 damage to a target player")
    void preventsNextTwoDamageToPlayer() {
        addRedeemerReady();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents the next 2 damage to a target creature")
    void preventsNextTwoDamageToCreature() {
        addRedeemerReady();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addRedeemerReady();
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addRedeemerReady() {
        addCreatureReady(player1, new AvenRedeemer());
    }
}
