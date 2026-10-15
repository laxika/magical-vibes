package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.cards.n.NorikaYamazakiThePoet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RegentsAuthority.class, BearerOfMemory.class, NorikaYamazakiThePoet.class})
class RegentsAuthorityTest extends BaseCardTest {

    @Test
    @DisplayName("A normal creature gets +2/+2 until end of turn")
    void normalCreatureGetsPlusTwoPlusTwo() {
        Permanent target = addCreature("Normal creature");

        castOn(target);

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A legendary creature gets a counter and +1/+1 until end of turn")
    void legendaryCreatureGetsCounterAndTemporaryBoost() {
        Permanent target = addCreature("Legendary creature");
        TestCards.mutableCard(target).setSupertypes(Set.of(CardSupertype.LEGENDARY));

        castOn(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("An enchantment creature gets a counter and +1/+1 until end of turn")
    void enchantmentCreatureGetsCounterAndTemporaryBoost() {
        Permanent target = addCreature("Enchantment creature");
        TestCards.mutableCard(target).setAdditionalTypes(Set.of(CardType.ENCHANTMENT));

        castOn(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("An enchantment creature you control retains its counter after the boost expires")
    void ownEnchantmentCreatureRetainsCounterAfterCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());

        castOn(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A legendary creature without the enchantment type receives the upgraded effect")
    void realLegendaryCreatureReceivesUpgradedEffect() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NorikaYamazakiThePoet());

        castOn(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Being both legendary and an enchantment does not double the upgraded effect")
    void legendaryEnchantmentCreatureReceivesOnlyOneCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearerOfMemory());
        TestCards.mutableCard(target).setSupertypes(Set.of(CardSupertype.LEGENDARY));

        castOn(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The legendary condition is checked at resolution rather than when cast")
    void losingLegendaryBeforeResolutionUsesNormalBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NorikaYamazakiThePoet());
        harness.setHand(player1, List.of(new RegentsAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        TestCards.mutableCard(target).setSupertypes(Set.of());

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("A creature that leaves before resolution receives neither a counter nor a boost")
    void removedTargetReceivesNoEffect() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearerOfMemory());
        harness.setHand(player1, List.of(new RegentsAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setPower(2);
        card.setToughness(2);
        return harness.addToBattlefieldAndReturn(player2, card);
    }

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new RegentsAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
