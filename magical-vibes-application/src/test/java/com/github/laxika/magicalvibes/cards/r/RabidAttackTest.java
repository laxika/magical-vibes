package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RabidAttack.class, GrizzlyBears.class, Shock.class, Forest.class})
class RabidAttackTest extends BaseCardTest {

    @Test
    @DisplayName("Each targeted creature gets +1/+0 and a temporary death trigger")
    void boostsEachTargetedCreature() {
        Permanent bearA = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bearB = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RabidAttack()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, List.of(bearA.getId(), bearB.getId()));

        for (Permanent bear : List.of(bearA, bearB)) {
            assertThat(bear.getPowerModifier()).isEqualTo(1);
            assertThat(bear.getToughnessModifier()).isZero();
            assertThat(bear.getTemporaryTriggeredEffects(EffectSlot.ON_DEATH)).hasSize(1);
        }
    }

    @Test
    @DisplayName("A boosted creature dying draws a card for its controller")
    void deathDrawsCard() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new RabidAttack(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(bear.getId()));


        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bear.getId()));
        // The lone library card was drawn by the granted "when this dies, draw a card" trigger
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RabidAttack()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, List.of(bear.getId()));
        assertThat(bear.getPowerModifier()).isEqualTo(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance through cleanup

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getTemporaryTriggeredEffects(EffectSlot.ON_DEATH)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature you do not control")
    void cannotTargetOpponentCreature() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RabidAttack()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opponentBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May be cast with no targets")
    void resolvesWithNoTargets() {
        harness.setHand(player1, List.of(new RabidAttack()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, List.of());

        harness.assertInGraveyard(player1, "Rabid Attack");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Any number includes more than ninety-nine creatures")
    void canTargetOneHundredCreatures() {
        List<Permanent> bears = java.util.stream.IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()))
                .toList();
        harness.setHand(player1, List.of(new RabidAttack()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, bears.stream().map(Permanent::getId).toList());

        assertThat(bears).allSatisfy(bear -> assertThat(bear.getPowerModifier()).isEqualTo(1));
    }

    @Test
    @DisplayName("A removed target does not prevent the remaining target from receiving both effects")
    void remainingTargetStillGetsBothEffects() {
        Permanent bearA = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bearB = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new RabidAttack(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, List.of(bearA.getId(), bearB.getId()));
        harness.castAndResolveInstant(player1, 0, bearA.getId());
        harness.passBothPriorities();

        assertThat(bearB.getPowerModifier()).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.castAndResolveInstant(player1, 0, bearB.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
    @Test
    @DisplayName("Two resolved copies grant two independent death draws")
    void repeatedCastsEachDrawACard() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new RabidAttack(), new RabidAttack(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(bear.getId()));
        harness.castAndResolveInstant(player1, 0, List.of(bear.getId()));
        assertThat(bear.getPowerModifier()).isEqualTo(2);
        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
