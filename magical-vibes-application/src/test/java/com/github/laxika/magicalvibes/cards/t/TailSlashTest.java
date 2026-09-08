package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TailSlash.class, AirElemental.class, GrizzlyBears.class})
class TailSlashTest extends BaseCardTest {

    @Test
    void controlledCreatureDealsDamageEqualToItsPower() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TailSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void sourceMustBeACreatureYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TailSlash()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(opponentCreature.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void targetMustBeACreatureAnOpponentControls() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new TailSlash()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(source.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
