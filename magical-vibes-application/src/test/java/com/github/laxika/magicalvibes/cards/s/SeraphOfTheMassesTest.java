package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SeraphOfTheMassesTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of creatures you control")
    void powerAndToughnessEqualControlledCreatures() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfTheMasses());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can be cast using convoke")
    void castsWithConvoke() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeraphOfTheMasses()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID convokeCreatureId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convokeCreatureId));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();

        harness.passBothPriorities();

        Permanent seraph = findPermanent(player1, "Seraph of the Masses");
        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(2);
    }
}
