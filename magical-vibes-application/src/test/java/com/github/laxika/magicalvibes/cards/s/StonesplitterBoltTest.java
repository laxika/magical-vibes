package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StonesplitterBolt.class, DarksteelRelic.class, HillGiant.class})
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
