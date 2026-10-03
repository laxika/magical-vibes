package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BogslithersEmbrace.class, GrizzlyBears.class, Forest.class})
class BogslithersEmbraceTest extends BaseCardTest {

    @Test
    void blightsACreatureAsTheAdditionalCostAndExilesTheTarget() {
        Permanent costCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new BogslithersEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, targetId, costCreature.getId());
        assertThat(costCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void paysManaInsteadOfBlighting() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new BogslithersEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithSacrifice(player1, 0, targetId, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void cannotCastWithoutACreatureOrManaForTheAdditionalCost() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new BogslithersEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("put counters on a creature you control or pay");
    }

    @Test
    void cannotTargetANonCreaturePermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        harness.setHand(player1, List.of(new BogslithersEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    void canBlightTheSameCreatureThatWillBeExiled() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BogslithersEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, creature.getId(), creature.getId());

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void cannotBlightAnOpponentsCreatureEvenWhenTheManaOptionIsAffordable() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BogslithersEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, creature.getId(), creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotBlightANonCreatureYouControl() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new BogslithersEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, targetId, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a creature");
        assertThat(land.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
