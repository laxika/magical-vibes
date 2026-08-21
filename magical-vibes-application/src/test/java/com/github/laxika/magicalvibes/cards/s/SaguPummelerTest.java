package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaguPummeler.class, GrizzlyBears.class, Mountain.class})
class SaguPummelerTest extends BaseCardTest {

    private void readyRenew() {
        harness.setGraveyard(player1, List.of(new SaguPummeler()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Renew puts two +1/+1 counters and a reach counter on target creature")
    void renewPutsCountersOnTargetCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        readyRenew();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.REACH)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Renew exiles Sagu Pummeler as an activation cost")
    void renewExilesSource() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyRenew();

        harness.activateGraveyardAbility(player1, 0, bears.getId());

        harness.assertNotInGraveyard(player1, "Sagu Pummeler");
    }

    @Test
    @DisplayName("Renew requires a creature target")
    void renewRequiresCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        readyRenew();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Renew can only be activated at sorcery speed")
    void renewIsSorcerySpeedOnly() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new SaguPummeler()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
