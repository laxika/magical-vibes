package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarchOfSwirlingMist.class, GrizzlyBears.class, HillGiant.class, WindDrake.class})
class MarchOfSwirlingMistTest extends BaseCardTest {

    @Test
    void phasesOutUpToXTargetCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MarchOfSwirlingMist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantForXWithDiscards(
                player1, 0, 2, List.of(ownCreature.getId(), opposingCreature.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(ownCreature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opposingCreature);
    }

    @Test
    void exilingBlueCardsFromHandReducesGenericCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MarchOfSwirlingMist(), new WindDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantForXWithDiscards(player1, 0, 2, List.of(target.getId()), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .contains("Wind Drake");
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(target);
    }

    @Test
    void optionalHandExileCostOnlyAcceptsBlueCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MarchOfSwirlingMist(), new HillGiant()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(
                player1, 0, 2, List.of(target.getId()), List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
    }
}
