package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BlightedAgent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PithDriller.class, PuresteelPaladin.class, BlightedAgent.class})
class PithDrillerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts one -1/-1 counter on target creature")
    void etbPutsOneCounterOnTargetCreature() {
        harness.addToBattlefield(player2, new PuresteelPaladin());
        UUID paladinId = harness.getPermanentId(player2, "Puresteel Paladin");

        harness.setHand(player1, List.of(new PithDriller()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, paladinId);
        resolveAllTriggers();

        // Puresteel Paladin survives as a 1/1 with one -1/-1 counter.
        Permanent paladin = findPermanent(player2, "Puresteel Paladin");
        assertThat(paladin.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(paladin.getEffectivePower()).isEqualTo(1);
        assertThat(paladin.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB kills creature with 1 toughness")
    void etbKillsOneToughnessCreature() {
        harness.addToBattlefield(player2, new BlightedAgent());
        UUID targetId = harness.getPermanentId(player2, "Blighted Agent");

        harness.setHand(player1, List.of(new PithDriller()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Blighted Agent");
        harness.assertInGraveyard(player2, "Blighted Agent");
    }

    @Test
    @DisplayName("Pith Driller enters battlefield regardless of ETB outcome")
    void pithDrillerEntersBattlefield() {
        harness.addToBattlefield(player2, new PuresteelPaladin());
        UUID paladinId = harness.getPermanentId(player2, "Puresteel Paladin");

        harness.setHand(player1, List.of(new PithDriller()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, paladinId);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Pith Driller");
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new PuresteelPaladin());
        UUID paladinId = harness.getPermanentId(player1, "Puresteel Paladin");

        harness.setHand(player1, List.of(new PithDriller()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, paladinId);
        resolveAllTriggers();

        Permanent paladin = findPermanent(player1, "Puresteel Paladin");
        assertThat(paladin.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can cast without target when no creatures on battlefield")
    void canCastWithoutTargetWhenNoCreatures() {
        harness.setHand(player1, List.of(new PithDriller()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pith Driller");
    }

    @Test
    @DisplayName("Must target itself when it enters an otherwise empty battlefield")
    void targetsItselfOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new PithDriller()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Pith Driller"));
        resolveAllTriggers();

        Permanent driller = findPermanent(player1, "Pith Driller");
        assertThat(driller.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(driller.getEffectivePower()).isEqualTo(1);
        assertThat(driller.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can choose Pith Driller itself after casting with another creature present")
    void choosesSelfAfterEnteringWithAnotherCreaturePresent() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player2, new PuresteelPaladin());
        harness.setHand(player1, List.of(new PithDriller()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Pith Driller"));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Pith Driller")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(paladin.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can pay four generic mana and two life and still resolve the ETB")
    void paysPhyrexianManaWithLife() {
        harness.addToBattlefield(player2, new BlightedAgent());
        harness.setHand(player1, List.of(new PithDriller()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, harness.getPermanentId(player2, "Blighted Agent"));
        harness.assertLife(player1, 18);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Pith Driller");
        harness.assertInGraveyard(player2, "Blighted Agent");
    }

    @Test
    @DisplayName("ETB still puts a counter on its target after Pith Driller leaves")
    void triggerResolvesWithoutSource() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player2, new PuresteelPaladin());
        harness.setHand(player1, List.of(new PithDriller()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, paladin.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();
        resolveAllTriggers();

        assertThat(paladin.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new PuresteelPaladin());
        UUID paladinId = harness.getPermanentId(player2, "Puresteel Paladin");

        harness.setHand(player1, List.of(new PithDriller()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, paladinId);
        harness.passBothPriorities(); // resolve creature spell and put ETB on stack

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB, which fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
