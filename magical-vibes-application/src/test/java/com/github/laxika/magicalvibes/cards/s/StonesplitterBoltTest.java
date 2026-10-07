package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.e.EdgewallPack;
import com.github.laxika.magicalvibes.cards.a.AshiokWickedManipulator;
import com.github.laxika.magicalvibes.cards.c.CandyTrail;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.UpTheBeanstalk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StonesplitterBolt.class, DarksteelRelic.class, HillGiant.class,
        AshiokWickedManipulator.class, CandyTrail.class, UpTheBeanstalk.class, EdgewallPack.class})
class StonesplitterBoltTest extends BaseCardTest {

    @Test
    void dealsXDamageWithoutBargain() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        target.setToughnessModifier(2);
        castBolt(target.getId());

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void dealsTwiceXDamageWhenBargained() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent target = addCreatureReady(player2, new HillGiant());
        target.setToughnessModifier(2);
        harness.setHand(player1, List.of(new StonesplitterBolt()));
        addMana();

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 3, target.getId(), null,
                List.of(), List.of(), false, sacrifice.getId(), null, null, null, null, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Darksteel Relic");
    }

    @Test
    void cannotTargetNonCreatureOrPlaneswalkerPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        harness.setHand(player1, List.of(new StonesplitterBolt()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 3, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or planeswalker");
    }

    @Test
    void marksExactlyXDamageWithoutBargain() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        target.setToughnessModifier(2);
        castBolt(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void removesXLoyaltyWithoutBargain() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshiokWickedManipulator());
        castBolt(target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Ashiok, Wicked Manipulator");
    }

    @Test
    void removesTwiceXLoyaltyWhenBargainedWithEnchantment() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new UpTheBeanstalk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshiokWickedManipulator());
        prepareBolt(2);
        castBargainedBolt(2, target.getId(), sacrifice.getId());

        harness.assertInGraveyard(player1, "Up the Beanstalk");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Ashiok, Wicked Manipulator");
    }

    @Test
    void bargainWithZeroXStillSacrificesButDealsNoDamage() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshiokWickedManipulator());
        prepareBolt(0);
        castBargainedBolt(0, target.getId(), sacrifice.getId());

        harness.assertInGraveyard(player1, "Candy Trail");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void cannotBargainBySacrificingOpponentsArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new CandyTrail());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshiokWickedManipulator());
        prepareBolt(2);

        assertThatThrownBy(() -> castBargainedBolt(2, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Candy Trail");
    }

    @Test
    void cannotBargainBySacrificingNontokenCreature() {
        Permanent sacrifice = addCreatureReady(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshiokWickedManipulator());
        prepareBolt(2);

        assertThatThrownBy(() -> castBargainedBolt(2, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    void cannotTargetPlayer() {
        prepareBolt(3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 3, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBargainBySacrificingCreatureToken() {
        harness.setHand(player1, List.of(new EdgewallPack()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        java.util.UUID ratId = harness.getPermanentId(player1, "Rat");
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshiokWickedManipulator());
        prepareBolt(2);
        castBargainedBolt(2, target.getId(), ratId);

        harness.assertNotOnBattlefield(player1, "Rat");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Edgewall Pack");
    }

    @Test
    void marksExactlyTwiceXDamageWhenBargained() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        prepareBolt(1);
        castBargainedBolt(1, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Edgewall Pack");
    }

    private void prepareBolt(int xValue) {
        harness.setHand(player1, List.of(new StonesplitterBolt()));
        harness.addMana(player1, ManaColor.RED, xValue + 1);
    }

    private void castBargainedBolt(int xValue, java.util.UUID targetId, java.util.UUID sacrificeId) {
        gs.playCard(gd, player1, 0, xValue, targetId, null,
                List.of(), List.of(), false, sacrificeId, null, null, null, null, true);
    }

    private void castBolt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new StonesplitterBolt()));
        addMana();
        harness.castInstant(player1, 0, 3, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 4);
    }
}
