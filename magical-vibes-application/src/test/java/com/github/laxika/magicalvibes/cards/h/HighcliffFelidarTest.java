package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HighcliffFelidar.class, GrizzlyBears.class, SerraAngel.class})
class HighcliffFelidarTest extends BaseCardTest {

    @Test
    void destroysTheGreatestPowerCreatureAnOpponentControls() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());

        castHighcliffFelidar();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Serra Angel");
    }

    @Test
    void controllerChoosesAmongCreaturesTiedForGreatestPower() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castHighcliffFelidar();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handlePermanentChosen(player1, first.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void doesNothingWhenAnOpponentControlsNoCreatures() {
        castHighcliffFelidar();

        harness.assertOnBattlefield(player1, "Highcliff Felidar");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castHighcliffFelidar() {
        harness.setHand(player1, List.of(new HighcliffFelidar()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
