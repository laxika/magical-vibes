package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeralIncarnation.class, RuneclawBear.class})
class FeralIncarnationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three 3/3 green Beast tokens")
    void createsThreeBeasts() {
        harness.setHand(player1, List.of(new FeralIncarnation()));
        harness.addMana(player1, ManaColor.GREEN, 9);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3);
        assertThat(battlefield).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getPower()).isEqualTo(3);
            assertThat(token.getCard().getToughness()).isEqualTo(3);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.BEAST);
            assertThat(token.getCard().getColors()).containsExactly(CardColor.GREEN);
            assertThat(token.isTapped()).isFalse();
        });
    }

    @Test
    @DisplayName("Convoke taps creatures to help pay the cost")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new FeralIncarnation()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("A summoning-sick green creature can convoke the green mana requirement")
    void convokePaysGreenRequirementWithSummoningSickCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        bear.setSummoningSick(true);
        harness.setHand(player1, List.of(new FeralIncarnation()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(bear.getId()));

        assertThat(bear.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Feral Incarnation");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
