package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.Wasteland;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Nexos.class, Forest.class, Wasteland.class, Fireball.class, GrizzlyBears.class})
class NexosTest extends BaseCardTest {

    @Test
    void basicLandGainsTwoXCostOnlyColorlessManaAbility() {
        harness.addToBattlefield(player1, new Nexos());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void xCostOnlyManaPaysXSpell() {
        harness.addToBattlefield(player1, new Nexos());
        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1, 1, 0, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Fireball()));

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isZero();
    }

    @Test
    void xCostOnlyManaCannotPayNonXSpell() {
        harness.addToBattlefield(player1, new Nexos());
        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1, 1, 0, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(2);
    }

    @Test
    void nonBasicLandDoesNotGainNexosAbility() {
        harness.addToBattlefield(player1, new Nexos());
        harness.addToBattlefield(player1, new Wasteland());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isZero();
    }
}
