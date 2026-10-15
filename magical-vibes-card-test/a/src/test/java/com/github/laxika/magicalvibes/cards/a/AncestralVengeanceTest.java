package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BattleBrawler;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncestralVengeance.class, BattleBrawler.class})
class AncestralVengeanceTest extends BaseCardTest {

    @Test
    void resolvesAndPutsCounterOnTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BattleBrawler());
        harness.setHand(player1, List.of(new AncestralVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> creature.getId().equals(permanent.getAttachedTo()));
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void enchantedCreatureGetsMinusOneMinusOneUntilAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BattleBrawler());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AncestralVengeance());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void cannotTargetOpponentsCreatureForCounter() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new BattleBrawler());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BattleBrawler());
        harness.setHand(player1, List.of(new AncestralVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, enchantedCreature.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, enchantedCreature.getId());
        harness.passBothPriorities();

        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canEnchantOpponentsCreatureAndPutCounterOnDifferentCreature() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new BattleBrawler());
        Permanent host = harness.addToBattlefieldAndReturn(player2, new BattleBrawler());
        harness.setHand(player1, List.of(new AncestralVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(1);
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, recipient)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recipient)).isEqualTo(3);
    }

    @Test
    void canResolveWithoutAnyCreatureYouControl() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new BattleBrawler());
        harness.setHand(player1, List.of(new AncestralVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ancestral Vengeance");
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(1);
        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void counterRemainsAfterAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BattleBrawler());
        harness.setHand(player1, List.of(new AncestralVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof AncestralVengeance);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void auraDoesNotEnterOrTriggerWhenEnchantTargetDisappears() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new BattleBrawler());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new BattleBrawler());
        harness.setHand(player1, List.of(new AncestralVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, host.getId());
        gd.playerBattlefields.get(player2.getId()).remove(host);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ancestral Vengeance");
        harness.assertInGraveyard(player1, "Ancestral Vengeance");
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void counterTriggerResolvesAfterAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BattleBrawler());
        harness.setHand(player1, List.of(new AncestralVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof AncestralVengeance);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counterTriggerDoesNotRetargetWhenRecipientLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new BattleBrawler());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new BattleBrawler());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new BattleBrawler());
        harness.setHand(player1, List.of(new AncestralVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, recipient.getId());
        gd.playerBattlefields.get(player1.getId()).remove(recipient);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ancestral Vengeance");
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(1);
        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
