package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StarfieldOfNyx;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DictateOfHeliod.class, GrizzlyBears.class})
class DictateOfHeliodTest extends BaseCardTest {

    @Test
    void boostsCreaturesYouControl() {
        harness.addToBattlefield(player1, new DictateOfHeliod());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void doesNotBoostOpponentsCreatures() {
        harness.addToBattlefield(player1, new DictateOfHeliod());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void bonusIsRemovedWhenDictateLeavesTheBattlefield() {
        harness.addToBattlefield(player1, new DictateOfHeliod());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Dictate of Heliod"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
    @Test
    void canBeCastDuringOpponentsTurnAndBoostsCreaturesOnResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.castFromHand(player1, new DictateOfHeliod(), "{3}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dictate of Heliod");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void boostsCreaturesEnteringAfterItAndMultipleCopiesStack() {
        harness.addToBattlefield(player1, new DictateOfHeliod());
        harness.addToBattlefield(player1, new DictateOfHeliod());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    @CardUsed({StarfieldOfNyx.class})
    void animatedDictateReceivesItsOwnBonus() {
        harness.addToBattlefield(player1, new StarfieldOfNyx());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new DictateOfHeliod());
        }

        Permanent dictate = findPermanent(player1, "Dictate of Heliod");

        assertThat(gqs.isCreature(gd, dictate)).isTrue();
        assertThat(gqs.getEffectivePower(gd, dictate)).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, dictate)).isEqualTo(13);
    }
}
