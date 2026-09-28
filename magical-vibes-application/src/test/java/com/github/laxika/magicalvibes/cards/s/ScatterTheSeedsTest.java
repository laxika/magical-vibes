package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BrambleElemental;
import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScatterTheSeeds.class, BrambleElemental.class, GlassGolem.class})
class ScatterTheSeedsTest extends BaseCardTest {

    @Test
    void createsThreeSaprolings() {
        harness.castFromHand(player1, new ScatterTheSeeds(), "{3}{G}{G}");

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Saproling")).isEqualTo(3);
    }

    @Test
    void createsGreenOneOneSaprolings() {
        harness.castFromHand(player1, new ScatterTheSeeds(), "{3}{G}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(3)
                .allSatisfy(saproling -> {
                    assertThat(saproling.getEffectivePower()).isEqualTo(1);
                    assertThat(saproling.getEffectiveToughness()).isEqualTo(1);
                    assertThat(saproling.getCard().getColors()).containsExactly(CardColor.GREEN);
                    assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
                });
    }

    @Test
    void convokeTapsGreenCreaturesToPayForColoredMana() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new BrambleElemental());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new BrambleElemental());
        harness.setHand(player1, List.of(new ScatterTheSeeds()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));
        harness.passBothPriorities();

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(3);
    }

    @Test
    void convokeTapsColorlessCreatureToPayForGenericMana() {
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new GlassGolem());
        harness.setHand(player1, List.of(new ScatterTheSeeds()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convokeCreature.getId()));
        assertThat(convokeCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Saproling")).isEqualTo(3);
    }
}
