package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BrambleElemental;
import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void convokeCanPayEntireCostWithSummoningSickCreatures() {
        List<Permanent> creatures = List.of(
                harness.addToBattlefieldAndReturn(player1, new BrambleElemental()),
                harness.addToBattlefieldAndReturn(player1, new BrambleElemental()),
                harness.addToBattlefieldAndReturn(player1, new GlassGolem()),
                harness.addToBattlefieldAndReturn(player1, new GlassGolem()),
                harness.addToBattlefieldAndReturn(player1, new GlassGolem()));
        creatures.forEach(creature -> creature.setSummoningSick(true));
        harness.setHand(player1, List.of(new ScatterTheSeeds()));

        harness.castInstantWithConvoke(player1, 0, List.of(),
                creatures.stream().map(Permanent::getId).toList());

        assertThat(creatures).allSatisfy(creature -> assertThat(creature.isTapped()).isTrue());
        assertThat(countPermanents(player1, "Saproling")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(3);
        assertThat(countPermanents(player2, "Saproling")).isZero();
    }

    @Test
    void tappedCreatureCannotConvoke() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BrambleElemental());
        creature.tap();
        harness.setHand(player1, List.of(new ScatterTheSeeds()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(creature.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Saproling")).isZero();
    }

    @Test
    void opponentsCreatureCannotConvoke() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BrambleElemental());
        harness.setHand(player1, List.of(new ScatterTheSeeds()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(creature.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void canCreateTokensDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.castFromHand(player1, new ScatterTheSeeds(), "{3}{G}{G}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Saproling")).isEqualTo(3);
        assertThat(countPermanents(player2, "Saproling")).isZero();
        assertThat(findPermanents(player1, "Saproling"))
                .allSatisfy(token -> assertThat(token.isTapped()).isFalse());
    }
}
